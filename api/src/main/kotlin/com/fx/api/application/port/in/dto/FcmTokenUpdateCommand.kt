package com.fx.api.application.port.`in`.dto

import com.fx.global.domain.DeviceType

data class FcmTokenUpdateCommand(
    val oldFcmToken: String,
    val newFcmToken: String,
    val deviceType: DeviceType
)