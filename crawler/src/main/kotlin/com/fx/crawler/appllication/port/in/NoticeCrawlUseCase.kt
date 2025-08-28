package com.fx.crawler.appllication.port.`in`

import com.fx.crawler.domain.CrawlableType
import com.fx.crawler.domain.Notice

interface NoticeCrawlUseCase {

    suspend fun crawlAndSaveNotices(
        crawlableTypes: List<CrawlableType>, page: Int = 1
    ): List<Notice>

}