package com.fx.api.application.port.`in`.dto

import com.fx.global.domain.DeviceType

data class FcmTokenCommand(
    val fcmToken: String,
    val deviceType: DeviceType
)