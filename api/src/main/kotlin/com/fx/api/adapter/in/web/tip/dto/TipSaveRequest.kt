package com.fx.api.adapter.`in`.web.tip.dto

import com.fx.api.application.port.`in`.tip.dto.TipSaveCommand
import com.fx.common.domain.DeviceType
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
    fun toCommand(): TipSaveCommand =
        TipSaveCommand(
            title = this.title,
            url = this.url,
            deviceType = this.deviceType
        )
}