package com.fx.api.adapter.`in`.web.dto

import com.fx.api.application.port.`in`.dto.FcmTokenCommand
import com.fx.global.domain.DeviceType

data class FcmTokenRequest(

    val fcmToken: String,
    val deviceType: DeviceType

) {
    fun toCommand() =
        FcmTokenCommand(
            fcmToken = this.fcmToken,
            deviceType = this.deviceType
        )
}