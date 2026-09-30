package com.fx.api.adapter.`in`.web.fcmtoken.dto

import com.fx.api.application.port.`in`.fcmtoken.dto.FcmTokenSaveCommand
import com.fx.common.domain.DeviceType
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