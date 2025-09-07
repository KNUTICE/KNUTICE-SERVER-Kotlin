package com.fx.global.adapter.feign

import com.fx.global.domain.TopicType
import java.time.LocalDate

data class NoticeNotificationFeignRequest(
    val nttId: Long,
    val title: String,
    val department: String,
    val contentUrl: String,
    val contentImageUrl: String?,
    val registrationDate: LocalDate,
    val isAttachment: Boolean,
    val topicType: TopicType,
    val type: String
)
