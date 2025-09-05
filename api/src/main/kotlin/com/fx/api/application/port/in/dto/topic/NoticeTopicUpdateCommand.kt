package com.fx.api.application.port.`in`.dto.topic

import com.fx.global.domain.NoticeType

data class NoticeTopicUpdateCommand (
    val fcmToken: String,
    val type: NoticeType,
    val enabled: Boolean
)