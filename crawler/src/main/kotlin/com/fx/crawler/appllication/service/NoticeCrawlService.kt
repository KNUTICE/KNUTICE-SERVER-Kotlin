package com.fx.crawler.appllication.service

import com.fx.crawler.appllication.port.`in`.NoticeCrawlUseCase
import com.fx.crawler.appllication.port.out.NoticeCrawlPort
import com.fx.crawler.appllication.port.out.NoticePersistencePort
import com.fx.global.application.port.out.WebhookPort
import com.fx.global.domain.CrawlableType
import com.fx.global.domain.Notice
import com.fx.global.domain.SlackMessage
import com.fx.global.domain.SlackType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class NoticeCrawlService(
    private val noticeCrawlPort: NoticeCrawlPort,
    private val noticePersistencePort: NoticePersistencePort,
    private val webhookPort: WebhookPort
): NoticeCrawlUseCase {

    private val log = LoggerFactory.getLogger(NoticeCrawlService::class.java)
    private val backgroundScope = CoroutineScope(Dispatchers.IO)


    override suspend fun crawlAndSaveNotices(
        topics: List<CrawlableType>,
        page: Int)
    : List<Notice> = coroutineScope {

        val allSummariesDeferred = (1..page).flatMap { p ->
            topics.map { topic ->
                async {
                    try {
                        log.info("Crawling page: {}, target: {}", p, topic)
                        noticeCrawlPort.crawlNoticeSummaries(topic, p)
                    } catch (e: Exception) {
                        log.error("크롤링 실패 target={} page={} : {}", topic, p, e.message)
                        backgroundScope.launch {
                            webhookPort.notifySlack(createSlackMessage(topic, e))
                        }
                        emptyList()
                    }
                }
            }
        }

        val allSummaries = allSummariesDeferred.awaitAll().flatten()
        log.info("Total summaries crawled: {}", allSummaries.size)

        // 전체 중복 제거
        val distinctSummaries = allSummaries.distinctBy { it.nttId }
        if (distinctSummaries.isEmpty()) return@coroutineScope emptyList()

        // DB에 이미 존재하는 nttId 확인
        val existingIds = noticePersistencePort.findByNttIdIn(distinctSummaries.map { it.nttId })
            .map { it.nttId }.toSet()

        val newNotices = distinctSummaries.filter { it.nttId !in existingIds }
        if (newNotices.isEmpty()) {
            log.info("No new notices found in all pages.")
            return@coroutineScope emptyList()
        }

        // 3. 신규 공지의 상세 조회 - 이미지 URL 크롤링 작업
        val detailedNotices = noticeCrawlPort.crawlNoticeDetails(newNotices)

        // 4. DB 저장
        noticePersistencePort.saveAll(detailedNotices)
        log.info("Saved {} new notices from pages 1..{}", detailedNotices.size, page)

        return@coroutineScope detailedNotices
    }

    private fun createSlackMessage(topic: CrawlableType, e: Exception): SlackMessage =
        SlackMessage.create(
            """
                *Topic* : $topic
                *Message* : ${e.message}
            """.trimIndent(),
            SlackType.CRAWL_ERROR
        )

}