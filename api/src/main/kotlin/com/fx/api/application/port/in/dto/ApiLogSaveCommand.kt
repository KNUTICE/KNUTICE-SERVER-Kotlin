package com.fx.api.application.port.`in`.dto

data class ApiLogSaveCommand(

    val urlPattern: String,

    val url: String,

    val method: String,

    val queryParameters: Map<String, String>? = null,

    val fcmToken: String? = null,

    val clientIp: String,

    val statusCode: Int,

    val executionTime: Long

)