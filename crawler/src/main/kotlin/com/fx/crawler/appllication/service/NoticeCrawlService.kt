package com.fx.crawler.appllication.service

import com.fx.crawler.appllication.port.`in`.NoticeCrawlUseCase
import com.fx.crawler.appllication.port.out.NoticeCrawlPort
import com.fx.crawler.appllication.port.out.NoticePersistencePort
import com.fx.crawler.domain.CrawlableType
import com.fx.crawler.domain.Notice
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.time.measureTimedValue

@Service
class NoticeCrawlService(
    private val noticeCrawlPort: NoticeCrawlPort,
    private val noticePersistencePort: NoticePersistencePort
): NoticeCrawlUseCase {

    private val log = LoggerFactory.getLogger(NoticeCrawlService::class.java)

    override suspend fun crawlAndSaveNotices(crawlableTypes: List<CrawlableType>, page: Int): List<Notice> {

        // 1. 목록 크롤링
        val crawlResult = measureTimedValue {
            crawlableTypes.flatMap {
                log.info("Crawl target : {}", it)
                noticeCrawlPort.crawlNoticeSummaries(it, page)
            }
        }
        val summaries = crawlResult.value.distinctBy { it.nttId } // Set
        log.info("Summaries crawl finished in {} ms", crawlResult.duration.inWholeMilliseconds)
        log.info("Total : {}", crawlResult.value.size)
        log.info("Distinct total : {}", summaries.size)

        if (summaries.isEmpty()) return emptyList()

        // 2. DB에 존재하는 nttId 조회
        val existingIds = noticePersistencePort.findByNttIdIn(summaries.map { it.nttId } )
            .map { it.nttId }.toSet()

        val newNotices = summaries.filter { it.nttId !in existingIds }

        if (newNotices.isEmpty()) {
            log.info("No new notices found.")
            return emptyList()
        }

        // 3. 신규 공지의 상세 조회 - 이미지 URL 크롤링 작업
        val detailedNoticesCrawlResult = measureTimedValue {
            noticeCrawlPort.crawlNoticeDetails(newNotices)
        }
        val detailedNotices = detailedNoticesCrawlResult.value
        log.info("Details crawl finished in {}", crawlResult.duration.inWholeMilliseconds)

        // 4. DB 저장
        noticePersistencePort.saveAll(detailedNotices)
        log.info("Saved {} new notices", detailedNotices.size)

        return detailedNotices
    }


}