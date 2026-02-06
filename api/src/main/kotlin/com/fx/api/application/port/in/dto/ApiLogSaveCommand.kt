package com.fx.api.application.port.`in`.dto

import com.fx.global.domain.DeviceType

data class ApiLogSaveCommand(

    val urlPattern: String,

    val url: String,

    val method: String,

    val queryParameters: Map<String, String>? = null,

    val fcmToken: String? = null,

    val clientIp: String,

    val deviceType: DeviceType,

    val statusCode: Int,

    val executionTime: Long

)