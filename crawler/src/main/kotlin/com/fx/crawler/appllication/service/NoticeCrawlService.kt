package com.fx.crawler.appllication.service

import com.fx.crawler.appllication.port.`in`.NoticeCrawlUseCase
import com.fx.crawler.appllication.port.out.NoticeCrawlPort
import com.fx.crawler.appllication.port.out.NoticePersistencePort
import com.fx.global.domain.CrawlableType
import com.fx.global.domain.Notice
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class NoticeCrawlService(
    private val noticeCrawlPort: NoticeCrawlPort,
    private val noticePersistencePort: NoticePersistencePort
): NoticeCrawlUseCase {

    private val log = LoggerFactory.getLogger(NoticeCrawlService::class.java)

    override suspend fun crawlAndSaveNotices(
        topics: List<CrawlableType>,
        page: Int)
    : List<Notice> = coroutineScope {

        val allSummariesDeferred = (1..page).flatMap { p ->
            topics.map { topic ->
                async {
                    log.info("Crawling page: {}, target: {}", p, topic)
                    noticeCrawlPort.crawlNoticeSummaries(topic, p)
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

}