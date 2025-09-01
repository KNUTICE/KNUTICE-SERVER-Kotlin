package com.fx.api.adapter.`in`.web.dto

import com.fx.api.domain.Tip
import com.fx.global.domain.DeviceType
import java.time.LocalDateTime

data class TipResponse(
    val tipId: String,
    val title: String,
    val url: String,
    val deviceType: DeviceType,
    val createdAt: LocalDateTime
) {

    companion object {
        fun from(tips: List<Tip>): List<TipResponse> =
            tips.map {
                TipResponse(
                    tipId = it.id!!,
                    title = it.title,
                    url = it.url,
                    deviceType = it.deviceType,
                    createdAt = it.createdAt!!
                )
            }
    }

}
