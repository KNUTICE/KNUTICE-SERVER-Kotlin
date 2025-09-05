package com.fx.api.application.port.`in`.dto

import com.fx.api.domain.TopicType

data class TopicUpdateCommand(
    val fcmToken: String,
    val topicType: TopicType,
    val topic: Enum<*>,
    val enabled: Boolean
)