package com.fx.api.application.port.`in`.dto

import com.fx.api.domain.TopicCategory

data class TopicUpdateCommand(
    val fcmToken: String,
    val category: TopicCategory,
    val topic: Enum<*>,
    val enabled: Boolean
)