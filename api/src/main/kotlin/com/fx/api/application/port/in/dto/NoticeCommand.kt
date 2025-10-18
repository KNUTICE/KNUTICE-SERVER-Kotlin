package com.fx.api.application.port.`in`.dto

import com.fx.global.domain.CrawlableType
import java.time.LocalDate

data class NoticeCommand(

    val nttId: Long,
    val title: String,
    val contentUrl: String,
    val contentSummary: String? = null,
    val contentImageUrl: String? = null,
    val department: String,
    val registrationDate: LocalDate,
    val isAttachment: Boolean,
    val topic: CrawlableType

)