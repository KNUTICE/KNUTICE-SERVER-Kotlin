package com.fx.api.adapter.`in`.web.dto.fcm

import com.fx.api.application.port.`in`.dto.FcmTokenUpdateCommand
import com.fx.global.domain.DeviceType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class FcmTokenUpdateRequest(

    @field:NotBlank
    val newFcmToken: String,

    @field:NotNull
    val deviceType: DeviceType

) {

    fun toCommand(oldFcmToken: String) =
        FcmTokenUpdateCommand(
            oldFcmToken = oldFcmToken,
            newFcmToken = this.newFcmToken,
            deviceType = this.deviceType
        )

}
