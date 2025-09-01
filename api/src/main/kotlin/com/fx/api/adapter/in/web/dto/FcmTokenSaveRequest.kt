package com.fx.api.adapter.`in`.web.dto

import com.fx.api.application.port.`in`.dto.FcmTokenCommand
import com.fx.global.domain.DeviceType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class FcmTokenSaveRequest(

    @field:NotBlank
    val fcmToken: String,

    @field:NotNull
    val deviceType: DeviceType

) {
    fun toCommand() =
        FcmTokenCommand(
            fcmToken = this.fcmToken,
            deviceType = this.deviceType
        )
}