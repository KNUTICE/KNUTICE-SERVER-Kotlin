package com.fx.api.adapter.`in`.web.dto.fcm

import com.fx.api.application.port.`in`.dto.FcmTokenSaveCommand
import com.fx.global.domain.DeviceType
import jakarta.validation.constraints.NotNull

data class FcmTokenSaveRequest(

    @field:NotNull
    val deviceType: DeviceType

) {
    fun toCommand(fcmToken: String) =
        FcmTokenSaveCommand(
            fcmToken = fcmToken,
            deviceType = this.deviceType
        )
}