package com.fx.crawler.appllication.service

import com.fx.crawler.appllication.port.`in`.NoticeSummaryUseCase
import com.fx.crawler.appllication.port.out.NoticeCrawlPort
import com.fx.crawler.appllication.port.out.NoticePersistencePort
import com.fx.crawler.appllication.port.out.NoticeSummaryPort
import com.fx.global.domain.NoticeQuery
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class NoticeSummaryService(
    private val noticeSummaryPort: NoticeSummaryPort,
    private val noticeCrawlPort: NoticeCrawlPort,
    private val noticePersistencePort: NoticePersistencePort,
) : NoticeSummaryUseCase {

    private companion object {
        const val BATCH_SIZE = 5
    }

    private val log = LoggerFactory.getLogger(NoticeSummaryService::class.java)

    private var cursor: Long? = null

    override suspend fun summarizeNotice() {
        // 1. 공지 조회
        val query = NoticeQuery(
            nttId = cursor,
            pageable = PageRequest.of(0, BATCH_SIZE, Sort.by(Sort.Direction.DESC, "nttId"))
        )
        val notices = noticePersistencePort.getNotices(query)
        if (notices.isEmpty()) return

        // 2. content 없는 공지만 크롤링
        val toCrawl = notices.filter { it.content.isNullOrBlank() }
        val crawledNotices = if (toCrawl.isNotEmpty()) {
            noticeCrawlPort.crawlNoticeDetails(toCrawl)
        } else emptyList()

        // 3. content는 있지만 contentSummary가 없는 공지만 AI 요약
        val toSummarize = (notices - crawledNotices).filter {
            it.content != null && it.contentSummary.isNullOrBlank()
        } + crawledNotices.filter { it.contentSummary.isNullOrBlank() }

        val summarizedNotices = if (toSummarize.isNotEmpty()) {
            noticeSummaryPort.summarizeNotices(toSummarize)
        } else emptyList()

        log.info("----- Summary Completed (${summarizedNotices.size} items) -----")
        summarizedNotices.forEach {
            log.info("[nttId=${it.nttId}] [title=${it.title}] [hasSummary=${!it.contentSummary.isNullOrBlank()}]")
        }
        noticePersistencePort.saveAll(summarizedNotices)


        // 4. cursor 업데이트
        cursor = notices.last().nttId
    }

}