package com.fx.api.domain

import com.fx.api.application.port.`in`.dto.TipCommand
import com.fx.global.domain.DeviceType
import java.time.LocalDateTime

data class Tip(
    val id: String? = null,
    val title: String,
    val url: String,
    val deviceType: DeviceType,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    companion object {
        fun createTip(tipCommand: TipCommand): Tip =
            Tip(
                title = tipCommand.title,
                url = tipCommand.url,
                deviceType = tipCommand.deviceType,
            )
    }
}
