package com.fx.api.adapter.`in`.web.dto

import com.fx.api.application.port.`in`.dto.TipCommand
import com.fx.global.domain.DeviceType

data class TipRequest(
    val title: String,
    val url: String,
    val deviceType: DeviceType
) {
    fun toCommand(): TipCommand =
        TipCommand(
            title = this.title,
            url = this.url,
            deviceType = this.deviceType
        )
}