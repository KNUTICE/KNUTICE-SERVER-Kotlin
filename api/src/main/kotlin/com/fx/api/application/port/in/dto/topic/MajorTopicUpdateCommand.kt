package com.fx.api.application.port.`in`.dto.topic

import com.fx.global.domain.MajorType

data class MajorTopicUpdateCommand (
    val fcmToken: String,
    val type: MajorType,
    val enabled: Boolean
)