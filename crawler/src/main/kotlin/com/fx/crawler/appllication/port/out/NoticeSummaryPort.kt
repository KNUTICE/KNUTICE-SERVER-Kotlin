package com.fx.crawler.appllication.port.out

import com.fx.global.domain.Notice

interface NoticeSummaryPort {

    suspend fun summarizeNotices(notices: List<Notice>): List<Notice>

}