package com.fx.api.application.port.`in`.tip.dto

import com.fx.common.domain.DeviceType

data class TipSaveCommand(
    val title: String,
    val url: String,
    val deviceType: DeviceType
)