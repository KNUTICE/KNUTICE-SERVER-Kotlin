package com.fx.crawler.appllication.port.out

import com.fx.global.domain.CrawlableType
import com.fx.global.domain.Notice

interface NoticeCrawlPort {

    /**
     * @return contentImage 를 제외한 Notice
     */
    suspend fun crawlNoticeSummaries(topic: CrawlableType, page: Int): List<Notice>

    suspend fun crawlNoticeDetails(notices: List<Notice>): List<Notice>

}