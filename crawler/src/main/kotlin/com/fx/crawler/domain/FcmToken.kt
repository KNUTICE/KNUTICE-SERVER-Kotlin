package com.fx.crawler.domain

import java.time.LocalDateTime

data class FcmToken(
    val fcmToken: String,
    val subscribedTopics: Set<String> = emptySet(),
    val deviceType: DeviceType,
    val isActive: Boolean,

    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)