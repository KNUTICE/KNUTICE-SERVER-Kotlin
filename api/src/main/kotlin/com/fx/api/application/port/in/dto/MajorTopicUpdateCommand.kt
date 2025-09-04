package com.fx.api.application.port.`in`.dto

import com.fx.global.domain.MajorType

data class MajorTopicUpdateCommand (
    val fcmToken: String,
    val type: MajorType,
    val enabled: Boolean
)