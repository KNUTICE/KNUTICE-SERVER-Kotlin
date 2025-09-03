package com.fx.api.application.port.`in`.dto

import com.fx.global.domain.DeviceType

data class TipSaveCommand(
    val title: String,
    val url: String,
    val deviceType: DeviceType
)