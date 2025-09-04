package com.fx.api.application.port.`in`.dto

import com.fx.global.domain.NoticeType

data class NoticeTopicUpdateCommand (
    val fcmToken: String,
    val type: NoticeType,
    val enabled: Boolean
)