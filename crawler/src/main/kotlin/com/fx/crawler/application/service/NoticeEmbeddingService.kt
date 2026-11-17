package com.fx.crawler.application.service

import com.fx.common.application.port.`in`.CatalogQueryUseCase
import com.fx.common.application.port.out.WebhookPort
import com.fx.common.domain.SlackMessage
import com.fx.common.domain.SlackType
import com.fx.common.domain.notice.EmbeddingStatus
import com.fx.common.domain.notice.NoticeChunk
import com.fx.common.domain.notice.NoticeChunkType
import com.fx.crawler.application.port.`in`.NoticeEmbeddingUseCase
import com.fx.crawler.application.port.out.NoticeEmbeddingPersistencePort
import com.fx.crawler.application.port.out.NoticeEmbeddingPort
import com.fx.crawler.application.port.out.NoticeImagePort
import com.fx.crawler.application.port.out.NoticePersistencePort
import com.fx.crawler.config.CrawlerProperties
import com.fx.crawler.domain.embedding.EmbeddedChunk
import com.fx.crawler.domain.embedding.EmbeddingInput
import com.fx.crawler.domain.embedding.EmbeddingRejectedException
import com.fx.crawler.domain.embedding.EmbeddingResult
import com.fx.crawler.domain.embedding.EmbeddingTarget
import com.fx.crawler.domain.embedding.EmbeddingUnavailableException
import com.fx.crawler.domain.embedding.NoticeChunker
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

/**
 * 공지 RAG 임베딩. 공지 하나를 본문 조각 · 본문 이미지 청크로 나눠 임베딩하고, 그 공지의 청크를 통째로 바꾼다.
 *
 * - 학교 사이트 · 임베딩 호출이 도는 동안 DB 커넥션을 잡지 않도록 [embed] 에는 트랜잭션을 걸지 않고, 결과 반영([applyResults])만 트랜잭션으로 묶는다.
 * - 이미지는 DB 에 첫 장 주소만 있으므로 상세 페이지를 다시 읽어 얻는다. 페이지가 없으면 본문만 임베딩한다.
 * - 호출 한도에 걸렸거나 Gemini · 학교 사이트 장애로 요청을 멈춘 동안의 공지는 실패로 세지 않고 다음 실행으로 미룬다 ([EmbeddingResult.Deferred]).
 *   장애를 처음 만난 공지만 실패로 세므로, 장애가 길어져도 대기 공지가 모두 FAILED 가 되지 않는다.
 */
@Service
class NoticeEmbeddingService(
    private val catalogQueryUseCase: CatalogQueryUseCase,
    private val noticePersistencePort: NoticePersistencePort,
    private val noticeEmbeddingPersistencePort: NoticeEmbeddingPersistencePort,
    private val noticeImagePort: NoticeImagePort,
    private val noticeEmbeddingPort: NoticeEmbeddingPort,
    private val webhookPort: WebhookPort,
    private val properties: CrawlerProperties,
    private val clock: Clock,
) : NoticeEmbeddingUseCase {

    private val log = LoggerFactory.getLogger(NoticeEmbeddingService::class.java)

    override fun findTargets(beforeNttId: Long?, size: Int): List<EmbeddingTarget> {
        val retryBefore = LocalDateTime.now(clock).minus(properties.embedding.retryInterval)
        return noticeEmbeddingPersistencePort.findEmbeddingTargets(retryBefore, beforeNttId, size)
    }

    /**
     * 본문 조각 → 본문 이미지 순서로 임베딩한다.
     * - 본문을 학교 사이트를 읽기 전에 임베딩해, 호출 한도에 걸린 날에는 학교 사이트를 읽지 않는다.
     * - 모델이 받지 않은 이미지는 빼고, 남은 이미지로 청크를 다시 나눈다 (순서 번호 · 제목 청크 여부가 바뀔 수 있다).
     */
    override fun embed(target: EmbeddingTarget): EmbeddingResult =
        try {
            noticeEmbeddingPort.checkAvailable()
            val board = boardName(target)

            val bodyDrafts = NoticeChunker.chunk(board, target.title, target.content, emptyList()).filter {
                it.type == NoticeChunkType.BODY
            }
            val bodyVectors = embedTexts(bodyDrafts.map { it.content })
            val imageVectors = embedImages(fetchImages(target))

            val drafts = NoticeChunker.chunk(board, target.title, target.content, imageVectors.keys.toList())
            val remainingBodyVectors = ArrayDeque(bodyVectors)
            EmbeddingResult.Completed(
                target.noticeId,
                drafts.map { draft ->
                    val vector = when (draft.type) {
                        NoticeChunkType.BODY -> remainingBodyVectors.removeFirst()
                        NoticeChunkType.IMAGE -> imageVectors.getValue(requireNotNull(draft.imageUrl))
                        // 본문이 없고 남은 이미지도 없을 때만 생긴다
                        NoticeChunkType.TITLE -> embedTexts(listOf(draft.content)).single()
                    }
                    EmbeddedChunk(draft, vector)
                },
            )
        } catch (e: EmbeddingUnavailableException) {
            log.info("임베딩 미룸 - nttId: {}, {}", target.nttId, e.message)
            EmbeddingResult.Deferred(target.noticeId)
        } catch (e: Exception) {
            log.warn("임베딩 실패 - nttId: {}, {}", target.nttId, e.message)
            EmbeddingResult.Failed(target.noticeId, e.message ?: e.javaClass.simpleName)
        }

    /** 청크를 바꿔 넣고 상태를 바꾼다. 시도 한도에 닿아 포기한 공지는 Slack 으로 알린다. */
    @Transactional
    override fun applyResults(results: List<EmbeddingResult>) {
        val applicable = results.filterNot {
            it is EmbeddingResult.Deferred
        }
        if (applicable.isEmpty()) {
            return
        }
        // 그사이 삭제된 공지는 청크도 넣지 않는다
        val noticesById = noticePersistencePort.findAllByIds(applicable.map { it.noticeId }).associateBy {
            requireNotNull(it.id)
        }

        noticeEmbeddingPersistencePort.replaceChunks(
            applicable.filterIsInstance<EmbeddingResult.Completed>()
                .filter {
                    it.noticeId in noticesById
                }
                .associate { result ->
                    result.noticeId to result.chunks.map {
                        toEntity(result.noticeId, it)
                    }
                }
        )

        val givenUp = applicable.mapNotNull { result ->
            val notice = noticesById[result.noticeId] ?: return@mapNotNull null
            when (result) {
                is EmbeddingResult.Completed -> notice.completeEmbedding()
                is EmbeddingResult.Failed -> notice.failEmbedding(properties.embedding.maxAttempts)
                is EmbeddingResult.Deferred -> Unit
            }
            notice.takeIf {
                result is EmbeddingResult.Failed && it.embeddingStatus == EmbeddingStatus.FAILED
            }
        }

        if (givenUp.isNotEmpty()) {
            val content = givenUp.joinToString("\n") {
                "[nttId : ${it.nttId}] [topic : ${it.topicName}] [title : ${it.title}]"
            }
            webhookPort.notifySlack(SlackMessage.create(content, SlackType.EMBEDDING_ERROR))
        }
    }

    private fun embedTexts(texts: List<String>): List<FloatArray> {
        if (texts.isEmpty()) {
            return emptyList()
        }
        val vectors = noticeEmbeddingPort.embed(texts.map { EmbeddingInput.Text(it) })
        check(vectors.size == texts.size) {
            "임베딩 결과 ${vectors.size} 개가 입력 ${texts.size} 개와 다릅니다."
        }
        return vectors
    }

    /** 이미지를 한 장씩 임베딩한다. 모델이 받지 않은 이미지(400)는 빼고, 키는 이미지 주소 · 순서는 본문 순서다. */
    private fun embedImages(images: Map<String, EmbeddingInput.Image>): Map<String, FloatArray> =
        images.mapNotNull { (url, image) ->
            try {
                url to noticeEmbeddingPort.embed(listOf(image)).single()
            } catch (e: EmbeddingRejectedException) {
                log.info("임베딩 모델이 받지 않은 이미지라 건너뜀 - {} ({})", url, e.message)
                null
            }
        }.toMap()

    /**
     * 상세 페이지의 본문 이미지를 앞에서부터 [CrawlerProperties.Embedding.maxImagesPerNotice] 장까지 읽는다.
     * 아이콘 · 받지 않는 형식처럼 건너뛴 이미지는 장수에 세지 않되, 학교 사이트 요청이 끝없이 늘지 않도록 시도는 그 2배까지만 한다.
     * 키는 이미지 주소, 순서는 본문 순서다.
     */
    private fun fetchImages(target: EmbeddingTarget): Map<String, EmbeddingInput.Image> {
        val maxImages = properties.embedding.maxImagesPerNotice
        return noticeImagePort.findImageUrls(target.contentUrl)
            .asSequence()
            .take(maxImages * 2)
            .mapNotNull { url ->
                noticeImagePort.fetchImage(url)?.let {
                    url to it
                }
            }
            .take(maxImages)
            .toMap()
    }

    /** 청크 앞에 붙이는 게시판 이름. 카탈로그의 한국어 이름을 쓰고, 토픽이 없어졌으면 저장된 토픽 이름을 쓴다. */
    private fun boardName(target: EmbeddingTarget): String =
        catalogQueryUseCase.getTopicCatalog().findByCode(target.topicCode)?.displayName?.ko ?: target.topicName

    private fun toEntity(noticeId: Long, chunk: EmbeddedChunk): NoticeChunk =
        NoticeChunk(
            noticeId = noticeId,
            chunkIndex = chunk.draft.index,
            chunkType = chunk.draft.type,
            content = chunk.draft.content,
            imageUrl = chunk.draft.imageUrl,
            embedding = chunk.vector,
            embeddingModel = noticeEmbeddingPort.modelTag,
        )

}
