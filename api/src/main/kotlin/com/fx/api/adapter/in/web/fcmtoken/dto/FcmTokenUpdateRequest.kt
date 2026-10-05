package com.fx.api.adapter.`in`.web.fcmtoken.dto

import com.fx.api.application.port.`in`.fcmtoken.dto.FcmTokenUpdateCommand
import com.fx.common.domain.DeviceType
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
