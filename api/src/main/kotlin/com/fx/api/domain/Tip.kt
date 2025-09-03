package com.fx.api.domain

import com.fx.api.application.port.`in`.dto.TipSaveCommand
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
        fun createTip(tipSaveCommand: TipSaveCommand): Tip =
            Tip(
                title = tipSaveCommand.title,
                url = tipSaveCommand.url,
                deviceType = tipSaveCommand.deviceType,
            )
    }
}
