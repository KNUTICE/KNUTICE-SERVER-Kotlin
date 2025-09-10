package com.fx.api.adapter.`in`.web.dto.fcm

import com.fx.api.application.port.`in`.dto.FcmTokenUpdateCommand
import com.fx.global.domain.DeviceType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class FcmTokenUpdateRequest(

    @field:NotBlank
    val oldFcmToken: String,

    @field:NotNull
    val deviceType: DeviceType

) {

    fun toCommand(newFcmToken: String) =
        FcmTokenUpdateCommand(
            oldFcmToken = oldFcmToken,
            newFcmToken = newFcmToken,
            deviceType = this.deviceType
        )

}
