package com.fx.crawler.application.service

import com.fx.common.application.port.`in`.CatalogQueryUseCase
import com.fx.common.application.port.out.WebhookPort
import com.fx.common.concurrent.BoundedParallelExecutor
import com.fx.common.domain.SlackMessage
import com.fx.common.domain.SlackType
import com.fx.common.domain.TopicType
import com.fx.common.domain.notice.NOTICE_URL_MAX_LENGTH
import com.fx.crawler.application.port.`in`.NoticeCrawlUseCase
import com.fx.crawler.application.port.out.NoticeCrawlPort
import com.fx.crawler.application.port.out.NoticePersistencePort
import com.fx.crawler.config.CrawlerProperties
import com.fx.crawler.domain.crawl.CrawledNotice
import com.fx.crawler.domain.crawl.NoticeDetail
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

/**
 * 게시판 목록 → 신규 판별 → 상세 → 저장.
 * 학교 사이트 요청은 동시 실행 상한 안에서 병렬로 보내고, 요청이 도는 동안 DB 커넥션을 잡지 않도록 트랜잭션을 걸지 않는다.
 */
@Service
class NoticeCrawlService(
    private val catalogQueryUseCase: CatalogQueryUseCase,
    private val noticeCrawlPort: NoticeCrawlPort,
    private val noticePersistencePort: NoticePersistencePort,
    private val webhookPort: WebhookPort,
    @param:Qualifier("schoolSiteExecutor") private val schoolSiteExecutor: BoundedParallelExecutor,
    private val properties: CrawlerProperties,
) : NoticeCrawlUseCase {

    private val log = LoggerFactory.getLogger(NoticeCrawlService::class.java)

    override fun crawlAndSave(topicType: TopicType): Int {
        val topics = catalogQueryUseCase.getTopicCatalog().topicsOf(topicType).filter { it.crawlEnabled }
        if (topics.isEmpty()) {
            return 0
        }

        val listResults = schoolSiteExecutor.invokeAll(
            topics.map { topic -> { noticeCrawlPort.fetchNoticeList(topic) } },
            properties.crawl.timeout,
        )
        val failures = topics.zip(listResults).mapNotNull { (topic, result) ->
            result.exceptionOrNull()?.let { "${topic.name} : ${it.message}" }
        }
        if (failures.isNotEmpty()) {
            log.error("게시판 크롤링 실패 {}건 : {}", failures.size, failures)
            webhookPort.notifySlack(SlackMessage.create(failures.joinToString("\n"), SlackType.CRAWL_ERROR))
        }

        val crawled = listResults.flatMap { it.getOrDefault(emptyList()) }
            .distinctBy { it.nttId }
            .filter { isStorable(it) }
        if (crawled.isEmpty()) {
            return 0
        }

        val existingNttIds = noticePersistencePort.findExistingNttIds(crawled.map { it.nttId })
        val newNotices = crawled.filterNot { it.nttId in existingNttIds }
        if (newNotices.isEmpty()) {
            return 0
        }

        val detailResults = schoolSiteExecutor.invokeAll(
            newNotices.map { notice -> { noticeCrawlPort.fetchNoticeDetail(notice.contentUrl) } },
            properties.crawl.timeout,
        )
        val detailedNotices = newNotices.zip(detailResults) { notice, result ->
            val detail = result.getOrElse {
                log.warn("공지 상세 크롤링 실패 - nttId: {}, {}", notice.nttId, it.message)
                NoticeDetail.EMPTY
            }
            notice.withDetail(detail)
        }

        noticePersistencePort.saveCrawled(detailedNotices)
        detailedNotices.forEach {
            log.info("새 공지 - topic: {}, nttId: {}, title: {}", it.topic.name, it.nttId, it.title)
        }
        return detailedNotices.size
    }

    /** URL 이 컬럼 길이를 넘으면 잘라 저장할 수 없으므로 뺀다. */
    private fun isStorable(notice: CrawledNotice): Boolean {
        if (notice.contentUrl.length > NOTICE_URL_MAX_LENGTH) {
            log.warn("원문 URL 이 너무 길어 저장하지 않습니다 - nttId: {}", notice.nttId)
            return false
        }
        return true
    }

}
