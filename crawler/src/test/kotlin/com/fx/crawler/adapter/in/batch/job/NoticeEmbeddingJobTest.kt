package com.fx.crawler.adapter.`in`.batch.job

import com.fx.common.adapter.out.persistence.repository.NoticeChunkRepository
import com.fx.common.adapter.out.persistence.repository.NoticeContentRepository
import com.fx.common.adapter.out.persistence.repository.NoticeRepository
import com.fx.common.domain.SlackType
import com.fx.common.domain.TopicType
import com.fx.common.domain.catalog.TopicView
import com.fx.common.domain.i18n.LocalizedText
import com.fx.common.domain.notice.EMBEDDING_DIMENSIONS
import com.fx.common.domain.notice.EmbeddingStatus
import com.fx.common.domain.notice.Notice
import com.fx.common.domain.notice.NoticeChunk
import com.fx.common.domain.notice.NoticeChunkType
import com.fx.common.domain.notice.NoticeContent
import com.fx.crawler.domain.embedding.EmbeddingInput
import com.fx.crawler.support.CrawlerIntegrationTest
import com.fx.crawler.support.FakeNoticeCrawlPort
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.job.Job
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import java.time.LocalDate
import java.time.LocalDateTime

class NoticeEmbeddingJobTest : CrawlerIntegrationTest() {

    @Autowired @Qualifier("noticeEmbeddingJob") lateinit var noticeEmbeddingJob: Job
    @Autowired lateinit var noticeRepository: NoticeRepository
    @Autowired lateinit var noticeContentRepository: NoticeContentRepository
    @Autowired lateinit var noticeChunkRepository: NoticeChunkRepository

    /** Flyway 시드의 토픽 1 = 일반소식 */
    private val generalNews = topic(1, "GENERAL_NEWS")

    /** 카탈로그에 없는 토픽. 게시판 이름은 저장된 토픽 이름을 쓴다 */
    private val removedBoard = topic(999, "REMOVED_BOARD")

    private val imageA = "https://www.ut.ac.kr/upload/a.png"
    private val icon = "https://www.ut.ac.kr/upload/icon.gif"
    private val imageB = "https://www.ut.ac.kr/upload/b.png"

    @Test
    fun `대기 공지를 본문 · 본문 이미지 청크로 임베딩해 저장하고, 최신 게시글부터 처리한다`() {
        saveNotice(100, generalNews, content = "본문 100")
        saveNotice(101, generalNews, content = null)
        saveNotice(102, removedBoard, content = "본문 102")
        noticeImagePort.pages[FakeNoticeCrawlPort.contentUrl(100)] = listOf(imageA, icon, imageB)
        noticeImagePort.images[imageA] = image()
        noticeImagePort.images[imageB] = image()

        val execution = run(noticeEmbeddingJob)

        assertThat(execution.status).isEqualTo(BatchStatus.COMPLETED)
        listOf(100L, 101L, 102L).forEach {
            assertThat(statusOf(it)).isEqualTo(EmbeddingStatus.COMPLETED)
        }
        // 건너뛴 이미지(icon)는 빼고 본문 다음에 본문 순서대로
        assertThat(chunksOf(100).map { Triple(it.chunkType, it.content, it.imageUrl) }).containsExactly(
            Triple(NoticeChunkType.BODY, "[일반소식] 공지 100\n본문 100", null),
            Triple(NoticeChunkType.IMAGE, "[일반소식] 공지 100", imageA),
            Triple(NoticeChunkType.IMAGE, "[일반소식] 공지 100", imageB),
        )
        assertThat(chunksOf(100)[1].embedding[EMBEDDING_DIMENSIONS - 1]).isEqualTo(1f)
        assertThat(chunksOf(101).map { it.chunkType to it.content }).containsExactly(NoticeChunkType.TITLE to "[일반소식] 공지 101")
        assertThat(chunksOf(102).map { it.content }).containsExactly("[REMOVED_BOARD] 공지 102\n본문 102")
        assertThat(noticeChunkRepository.findAll()).allSatisfy {
            assertThat(it.embeddingModel).isEqualTo("fake-embedding@768")
            assertThat(it.embedding).hasSize(EMBEDDING_DIMENSIONS)
        }
        // 게시글 번호가 큰 공지부터. chunk 크기가 2 라 100 은 두 번째 페이지(ntt_id < 101)에서 읽는다
        assertThat(embeddedTexts()).containsExactly(
            "[REMOVED_BOARD] 공지 102\n본문 102",
            "[일반소식] 공지 101",
            "[일반소식] 공지 100\n본문 100",
        )
        // 이미지는 한 장씩 따로 보낸다
        assertThat(noticeEmbeddingPort.calls.filter { it.single() is EmbeddingInput.Image }).hasSize(2)
    }

    @Test
    fun `모델이 받지 않은 이미지는 빼고 남은 청크로 저장한다`() {
        val webp = "https://www.ut.ac.kr/upload/c.webp"
        saveNotice(100, generalNews, content = "본문 100")
        saveNotice(101, generalNews, content = null)
        noticeImagePort.pages[FakeNoticeCrawlPort.contentUrl(100)] = listOf(webp, imageA)
        noticeImagePort.pages[FakeNoticeCrawlPort.contentUrl(101)] = listOf(webp)
        noticeImagePort.images[webp] = EmbeddingInput.Image(byteArrayOf(1, 2, 3), "image/webp")
        noticeImagePort.images[imageA] = image()
        noticeEmbeddingPort.rejectedMimeTypes += "image/webp"

        run(noticeEmbeddingJob)

        assertThat(statusOf(100)).isEqualTo(EmbeddingStatus.COMPLETED)
        assertThat(chunksOf(100).map { Triple(it.chunkIndex, it.chunkType, it.imageUrl) }).containsExactly(
            Triple(0, NoticeChunkType.BODY, null),
            Triple(1, NoticeChunkType.IMAGE, imageA),
        )
        // 본문도 없고 남은 이미지도 없으면 제목 청크를 만든다
        assertThat(statusOf(101)).isEqualTo(EmbeddingStatus.COMPLETED)
        assertThat(chunksOf(101).map { it.chunkType to it.content }).containsExactly(NoticeChunkType.TITLE to "[일반소식] 공지 101")
    }

    @Test
    fun `학교 사이트 장애로 요청을 멈춘 동안의 공지는 시도 횟수를 세지 않고 미룬다`() {
        saveNotice(100, generalNews, content = "본문 100")
        noticeImagePort.paused = true

        run(noticeEmbeddingJob)

        assertThat(attemptsOf(100)).isEqualTo(EmbeddingStatus.PENDING to 0)
        assertThat(noticeChunkRepository.findAll()).isEmpty()
        // 본문만 임베딩해 놓고 버리지 않는다 (무료 한도를 헛되이 쓰지 않음)
        assertThat(noticeEmbeddingPort.calls).isEmpty()

        noticeImagePort.paused = false
        run(noticeEmbeddingJob)

        assertThat(statusOf(100)).isEqualTo(EmbeddingStatus.COMPLETED)
    }

    @Test
    fun `다시 대기로 돌린 공지는 옛 청크를 지우고 새 청크로 바꾼다`() {
        saveNotice(100, generalNews, content = "본문 100")
        noticeImagePort.pages[FakeNoticeCrawlPort.contentUrl(100)] = listOf(imageA)
        noticeImagePort.images[imageA] = image()
        run(noticeEmbeddingJob)
        assertThat(chunksOf(100)).hasSize(2)

        // 본문이 바뀌고 이미지가 빠진 뒤 다시 임베딩한다
        jdbcTemplate.update("UPDATE notice SET embedding_status = 'PENDING' WHERE ntt_id = 100")
        jdbcTemplate.update("UPDATE notice_content SET content = '새 본문' WHERE notice_id = ?", noticeIdOf(100))
        noticeImagePort.pages.clear()
        run(noticeEmbeddingJob)

        assertThat(statusOf(100)).isEqualTo(EmbeddingStatus.COMPLETED)
        assertThat(chunksOf(100).map { it.content }).containsExactly("[일반소식] 공지 100\n새 본문")
    }

    @Test
    fun `실패는 시도 횟수만 늘리고, 한도에 닿으면 포기하고 Slack 으로 알린다`() {
        saveNotice(100, generalNews, content = "본문 100")
        saveNotice(101, generalNews, content = "본문 101", failedAttempts = 2)
        saveNotice(102, generalNews, content = "본문 102", failedAttempts = 1)
        saveNotice(103, generalNews, content = "본문 103")
        // 101 은 마지막 시도가 재시도 간격 전, 102 는 방금 실패
        jdbcTemplate.update("UPDATE notice SET updated_at = ? WHERE ntt_id = 101", LocalDateTime.now().minusMinutes(31))
        noticeEmbeddingPort.failingTexts += listOf("본문 100", "본문 101")
        noticeImagePort.failingPages += FakeNoticeCrawlPort.contentUrl(103)

        val execution = run(noticeEmbeddingJob)

        assertThat(execution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(attemptsOf(100)).isEqualTo(EmbeddingStatus.PENDING to 1)
        assertThat(attemptsOf(101)).isEqualTo(EmbeddingStatus.FAILED to 3)
        // 방금 실패한 공지는 이번 실행에서 건너뛴다
        assertThat(attemptsOf(102)).isEqualTo(EmbeddingStatus.PENDING to 1)
        assertThat(embeddedTexts()).noneMatch {
            it.contains("본문 102")
        }
        // 학교 사이트 장애를 처음 만난 공지는 실패로 센다 (이미지 없이 저장하지 않는다)
        assertThat(attemptsOf(103)).isEqualTo(EmbeddingStatus.PENDING to 1)
        assertThat(noticeChunkRepository.findAll()).isEmpty()

        val alerts = webhookPort.messages.filter {
            it.type == SlackType.EMBEDDING_ERROR
        }
        assertThat(alerts).hasSize(1)
        assertThat(alerts.single().content).contains("nttId : 101").doesNotContain("nttId : 100")
    }

    @Test
    fun `호출 한도에 걸리면 상태와 시도 횟수를 그대로 두고 다음 실행으로 미룬다`() {
        saveNotice(100, generalNews, content = "본문 100")
        noticeEmbeddingPort.rateLimited = true

        val execution = run(noticeEmbeddingJob)

        assertThat(execution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(attemptsOf(100)).isEqualTo(EmbeddingStatus.PENDING to 0)
        assertThat(noticeChunkRepository.findAll()).isEmpty()
        // 한도에 걸린 동안에는 학교 사이트도 읽지 않는다
        assertThat(noticeImagePort.visitedPages).isEmpty()
        assertThat(webhookPort.messages.filter {
            it.type == SlackType.EMBEDDING_ERROR
        }).isEmpty()

        noticeEmbeddingPort.rateLimited = false
        run(noticeEmbeddingJob)

        assertThat(statusOf(100)).isEqualTo(EmbeddingStatus.COMPLETED)
        assertThat(chunksOf(100)).hasSize(1)
    }

    private fun saveNotice(nttId: Long, topic: TopicView, content: String?, failedAttempts: Int = 0) {
        val notice = Notice.crawled(
            nttId = nttId,
            topic = topic,
            title = "공지 $nttId",
            department = "학사팀",
            contentUrl = FakeNoticeCrawlPort.contentUrl(nttId),
            contentImageUrl = null,
            registrationDate = LocalDate.of(2026, 11, 16),
            isAttachment = false,
        )
        repeat(failedAttempts) {
            notice.failEmbedding(maxAttempts = 3)
        }
        val saved = noticeRepository.save(notice)
        noticeContentRepository.save(NoticeContent(requireNotNull(saved.id), content, null))
    }

    private fun noticeIdOf(nttId: Long): Long =
        requireNotNull(noticeRepository.findByNttId(nttId)!!.id)

    private fun statusOf(nttId: Long): EmbeddingStatus =
        noticeRepository.findByNttId(nttId)!!.embeddingStatus

    private fun attemptsOf(nttId: Long): Pair<EmbeddingStatus, Int> =
        noticeRepository.findByNttId(nttId)!!.let {
            it.embeddingStatus to it.embeddingAttemptCount
        }

    private fun chunksOf(nttId: Long): List<NoticeChunk> =
        noticeChunkRepository.findAllByNoticeIdOrderByChunkIndex(noticeIdOf(nttId))

    /** 임베딩 요청으로 보낸 텍스트 (보낸 순서) */
    private fun embeddedTexts(): List<String> =
        noticeEmbeddingPort.calls.flatten().filterIsInstance<EmbeddingInput.Text>().map {
            it.text
        }

    private fun image() =
        EmbeddingInput.Image(byteArrayOf(1, 2, 3), "image/png")

    private fun topic(code: Int, name: String) =
        TopicView(code.toLong(), code, name, TopicType.NOTICE, LocalizedText(name), null, "https://www.ut.ac.kr", "/", true, true)

}
