package com.fx.api.application.port.out.dto

import com.fx.global.domain.CrawlableType
import com.fx.global.domain.TopicType

data class TopicUpdateQuery(
    val fcmToken: String,
    val topicType: TopicType,
    val topic: CrawlableType,
    val enabled: Boolean
)

