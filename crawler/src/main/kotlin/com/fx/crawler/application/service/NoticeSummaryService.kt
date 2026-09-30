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
import com.fx.crawler.domain.summary.SummaryResult
import com.fx.crawler.domain.summary.SummaryTarget
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 공지 AI 요약. 요약 호출이 도는 동안 DB 커넥션을 잡지 않도록 [summarize] 에는 트랜잭션을 걸지 않고,
 * 결과 반영([applyResults])만 트랜잭션으로 묶는다.
 */
@Service
class NoticeSummaryService(
    private val catalogQueryUseCase: CatalogQueryUseCase,
    private val noticePersistencePort: NoticePersistencePort,
    private val noticeSummaryPort: NoticeSummaryPort,
    private val webhookPort: WebhookPort,
    private val properties: CrawlerProperties,
) : NoticeSummaryUseCase {

    private val log = LoggerFactory.getLogger(NoticeSummaryService::class.java)

    override fun findTargets(topicType: TopicType, afterNoticeId: Long?, size: Int): List<SummaryTarget> {
        val topicCodes = catalogQueryUseCase.getTopicCatalog().topicsOf(topicType).map {
            it.code
        }
        if (topicCodes.isEmpty()) {
            return emptyList()
        }
        return noticePersistencePort.findSummaryTargets(topicCodes, afterNoticeId, size)
    }

    override fun summarize(target: SummaryTarget): SummaryResult {
        val content = target.content?.takeIf {
            it.isNotBlank()
        }
            ?: return SummaryResult.Skipped(target.noticeId)

        return try {
            SummaryResult.Completed(target.noticeId, noticeSummaryPort.summarize(content))
        } catch (e: Exception) {
            log.warn("AI 요약 실패 - nttId: {}, {}", target.nttId, e.message)
            SummaryResult.Failed(target.noticeId, e.message ?: e.javaClass.simpleName)
        }
    }

    /** 요약을 저장하고 상태를 바꾼다. 시도 한도에 닿아 포기한 공지는 Slack 으로 알린다. */
    @Transactional
    override fun applyResults(results: List<SummaryResult>) {
        if (results.isEmpty()) {
            return
        }
        val noticeIds = results.map {
            it.noticeId
        }
        val noticesById = noticePersistencePort.findAllByIds(noticeIds).associateBy {
            requireNotNull(it.id)
        }
        val contentsByNoticeId = noticePersistencePort.findContents(noticeIds).associateBy {
            it.noticeId
        }

        val givenUp = results.mapNotNull { result ->
            val notice = noticesById[result.noticeId] ?: return@mapNotNull null
            when (result) {
                is SummaryResult.Completed -> {
                    contentsByNoticeId[result.noticeId]?.changeSummary(result.summary)
                    notice.summaryChanged(hasSummary = true)
                }
                is SummaryResult.Skipped -> notice.skipSummary()
                is SummaryResult.Failed -> notice.failSummary(properties.summary.maxAttempts)
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

}
