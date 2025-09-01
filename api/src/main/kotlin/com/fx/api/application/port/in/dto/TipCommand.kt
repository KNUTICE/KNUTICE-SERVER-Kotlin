package com.fx.api.application.port.`in`.dto

import com.fx.global.domain.DeviceType

data class TipCommand(
    val title: String,
    val url: String,
    val deviceType: DeviceType
)