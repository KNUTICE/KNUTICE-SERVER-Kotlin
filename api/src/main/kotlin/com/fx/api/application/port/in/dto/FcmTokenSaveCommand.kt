package com.fx.api.application.port.`in`.dto

import com.fx.common.domain.DeviceType

data class FcmTokenSaveCommand(
    val fcmToken: String,
    val deviceType: DeviceType
)