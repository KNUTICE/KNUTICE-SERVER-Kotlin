package com.fx.api.application.port.`in`.dto

import com.fx.common.domain.DeviceType

data class FcmTokenUpdateCommand(
    val oldFcmToken: String,
    val newFcmToken: String,
    val deviceType: DeviceType
)