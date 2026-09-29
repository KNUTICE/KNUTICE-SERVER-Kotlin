package com.fx.crawler.appllication.port.`in`

import com.fx.common.domain.CrawlableType
import com.fx.common.domain.Notice

interface NoticeCrawlUseCase {

    suspend fun crawlAndSaveNotices(
        topics: List<CrawlableType>, page: Int = 1
    ): List<Notice>

}