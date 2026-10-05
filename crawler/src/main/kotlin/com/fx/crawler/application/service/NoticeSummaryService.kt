package com.fx.crawler.application.service

import com.fx.common.application.port.`in`.CatalogQueryUseCase
import com.fx.common.application.port.out.WebhookPort
import com.fx.common.domain.SlackMessage
import com.fx.common.domain.SlackType
import com.fx.common.domain.TopicType
import com.fx.common.domain.notice.SummaryStatus
import com.fx.crawler.application.port.`in`.NoticeSummaryUseCase
import com.fx.crawler.application.port.out.NoticePersistencePort
import com.fx.crawler.application.port.out.NoticeSummaryPort
import com.fx.crawler.config.CrawlerProperties
import com.fx.crawler.domain.summary.SummaryRateLimitedException
import com.fx.crawler.domain.summary.SummaryResult
import com.fx.crawler.domain.summary.SummaryTarget
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

/**
 * 공지 AI 요약. 요약 호출이 도는 동안 DB 커넥션을 잡지 않도록 [summarize] 에는 트랜잭션을 걸지 않고,
 * 결과 반영([applyResults])만 트랜잭션으로 묶는다.
 *
 * 호출 한도에 걸린 공지는 실패로 세지 않고 다음 실행으로 미룬다 ([SummaryResult.Deferred]).
 */
@Service
class NoticeSummaryService(
    private val catalogQueryUseCase: CatalogQueryUseCase,
    private val noticePersistencePort: NoticePersistencePort,
    private val noticeSummaryPort: NoticeSummaryPort,
    private val webhookPort: WebhookPort,
    private val properties: CrawlerProperties,
    private val clock: Clock,
) : NoticeSummaryUseCase {

    private val log = LoggerFactory.getLogger(NoticeSummaryService::class.java)

    override fun findTargets(afterNoticeId: Long?, size: Int): List<SummaryTarget> {
        val catalog = catalogQueryUseCase.getTopicCatalog()
        val topicCodes = SUMMARY_TOPIC_TYPES.flatMap {
            catalog.topicsOf(it)
        }.map {
            it.code
        }
        if (topicCodes.isEmpty()) {
            return emptyList()
        }
        val retryBefore = LocalDateTime.now(clock).minus(properties.summary.retryInterval)
        return noticePersistencePort.findSummaryTargets(topicCodes, retryBefore, afterNoticeId, size)
    }

    override fun summarize(target: SummaryTarget): SummaryResult {
        val content = target.content?.takeIf {
            it.isNotBlank()
        }
            ?: return SummaryResult.Skipped(target.noticeId)

        return try {
            SummaryResult.Completed(target.noticeId, noticeSummaryPort.summarize(content))
        } catch (e: SummaryRateLimitedException) {
            log.info("AI 요약 호출 한도로 미룸 - nttId: {}, {}", target.nttId, e.message)
            SummaryResult.Deferred(target.noticeId)
        } catch (e: Exception) {
            log.warn("AI 요약 실패 - nttId: {}, {}", target.nttId, e.message)
            SummaryResult.Failed(target.noticeId, e.message ?: e.javaClass.simpleName)
        }
    }

    /** 요약을 저장하고 상태를 바꾼다. 시도 한도에 닿아 포기한 공지는 Slack 으로 알린다. */
    @Transactional
    override fun applyResults(results: List<SummaryResult>) {
        val applicable = results.filterNot {
            it is SummaryResult.Deferred
        }
        if (applicable.isEmpty()) {
            return
        }
        val noticeIds = applicable.map {
            it.noticeId
        }
        val noticesById = noticePersistencePort.findAllByIds(noticeIds).associateBy {
            requireNotNull(it.id)
        }
        val contentsByNoticeId = noticePersistencePort.findContents(noticeIds).associateBy {
            it.noticeId
        }

        val givenUp = applicable.mapNotNull { result ->
            val notice = noticesById[result.noticeId] ?: return@mapNotNull null
            when (result) {
                is SummaryResult.Completed -> {
                    contentsByNoticeId[result.noticeId]?.changeSummary(result.summary)
                    notice.summaryChanged(hasSummary = true)
                }
                is SummaryResult.Skipped -> notice.skipSummary()
                is SummaryResult.Failed -> notice.failSummary(properties.summary.maxAttempts)
                is SummaryResult.Deferred -> Unit
            }
            notice.takeIf {
                result is SummaryResult.Failed && it.summaryStatus == SummaryStatus.FAILED
            }
        }

        if (givenUp.isNotEmpty()) {
            val content = givenUp.joinToString("\n") {
                "[nttId : ${it.nttId}] [topic : ${it.topicName}] [title : ${it.title}]"
            }
            webhookPort.notifySlack(SlackMessage.create(content, SlackType.AI_ERROR))
        }
    }

    companion object {
        /** 요약하는 게시판 유형. 학식에는 공지가 없다. */
        private val SUMMARY_TOPIC_TYPES = listOf(TopicType.NOTICE, TopicType.MAJOR)
    }

}
