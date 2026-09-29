package com.fx.api.application.port.`in`.dto

import com.fx.common.domain.TopicType
import com.fx.common.domain.CrawlableType

data class TopicUpdateCommand(
    val fcmToken: String,
    val topicType: TopicType,
    val topic: CrawlableType,
    val enabled: Boolean
)