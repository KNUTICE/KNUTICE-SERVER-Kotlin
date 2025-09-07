package com.fx.crawler.appllication.port.`in`.dto

import com.fx.global.domain.CrawlableType
import java.time.LocalDate

data class NoticeCommand(

    val nttId: Long,
    val title: String,
    val department: String,
    val contentUrl: String,
    val contentImageUrl: String? = null,
    val registrationDate: LocalDate,
    val isAttachment: Boolean,
    val type: CrawlableType

)