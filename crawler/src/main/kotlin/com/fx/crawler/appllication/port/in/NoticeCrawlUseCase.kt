package com.fx.crawler.appllication.port.`in`

import com.fx.global.domain.CrawlableType
import com.fx.global.domain.Notice

interface NoticeCrawlUseCase {

    suspend fun crawlAndSaveNotices(
        topics: List<CrawlableType>, page: Int = 1
    ): List<Notice>

}