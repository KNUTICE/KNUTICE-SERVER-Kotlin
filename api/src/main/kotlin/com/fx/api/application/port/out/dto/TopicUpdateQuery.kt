package com.fx.api.application.port.out.dto

import com.fx.common.domain.CrawlableType
import com.fx.common.domain.TopicType

data class TopicUpdateQuery(
    val fcmToken: String,
    val topicType: TopicType,
    val topic: CrawlableType,
    val enabled: Boolean
)

