package com.fx.api.adapter.`in`.web.dto.tip

import com.fx.api.domain.Tip
import com.fx.common.domain.DeviceType
import java.time.LocalDateTime

data class TipResponse(
    /** TSID 를 문자열로 내보낸다 (JS 숫자 정밀도 한계). */
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
                    tipId = requireNotNull(it.id).toString(),
                    title = it.title,
                    url = it.url,
                    deviceType = it.deviceType,
                    createdAt = requireNotNull(it.createdAt)
                )
            }
    }

}
