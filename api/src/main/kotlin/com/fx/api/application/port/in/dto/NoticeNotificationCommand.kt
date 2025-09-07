package com.fx.api.application.port.`in`.dto

import com.fx.global.domain.CrawlableType
import com.fx.global.domain.TopicType
import java.time.LocalDate

data class NoticeNotificationCommand(
    val fcmToken: String,
    val nttId: Long,
    val title: String,
    val department: String,
    val contentUrl: String,
    val contentImageUrl: String? = null,
    val registrationDate: LocalDate,
    val isAttachment: Boolean,
    val topicType: TopicType,
    val topic: CrawlableType,
)
