package com.fx.api.adapter.`in`.web.dto

import com.fx.api.application.port.`in`.dto.FcmTokenCommand
import com.fx.global.domain.DeviceType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class FcmTokenSaveRequest(

    @field:NotNull
    val deviceType: DeviceType

) {
    fun toCommand(fcmToken: String) =
        FcmTokenCommand(
            fcmToken = fcmToken,
            deviceType = this.deviceType
        )
}