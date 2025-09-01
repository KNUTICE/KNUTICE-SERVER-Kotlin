package com.fx.api.adapter.`in`.web.dto

import com.fx.api.application.port.`in`.dto.TipCommand
import com.fx.global.domain.DeviceType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class TipSaveRequest(

    @field:NotBlank
    val title: String,

    @field:NotBlank

    val url: String,

    @field:NotNull
    val deviceType: DeviceType
) {
    fun toCommand(): TipCommand =
        TipCommand(
            title = this.title,
            url = this.url,
            deviceType = this.deviceType
        )
}