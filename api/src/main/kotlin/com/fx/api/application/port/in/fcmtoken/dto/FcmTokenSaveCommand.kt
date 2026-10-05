package com.fx.api.application.port.`in`.fcmtoken.dto

import com.fx.common.domain.DeviceType

data class FcmTokenSaveCommand(
    val fcmToken: String,
    val deviceType: DeviceType
)