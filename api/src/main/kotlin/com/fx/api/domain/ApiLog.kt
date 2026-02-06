package com.fx.api.domain

import java.time.LocalDateTime

data class ApiLog (

    val id: String? = null,

    val urlPattern: String,

    val url: String,

    val method: String,

    val queryParameters: Map<String, String>? = null,

    val fcmToken: String? = null,

    val clientIp: String,

    val statusCode: Int,

    val executionTime: Long,

    val createdAt: LocalDateTime? = null,

    val updatedAt: LocalDateTime? = null

)