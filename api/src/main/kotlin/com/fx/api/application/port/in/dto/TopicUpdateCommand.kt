package com.fx.api.application.port.`in`.dto

import com.fx.global.domain.TopicType
import com.fx.global.domain.CrawlableType

data class TopicUpdateCommand(
    val fcmToken: String,
    val topicType: TopicType,
    val topic: CrawlableType,
    val enabled: Boolean
)