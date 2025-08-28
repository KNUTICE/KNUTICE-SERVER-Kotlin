package com.fx.crawler.domain

import java.time.LocalDateTime

data class FcmToken(
    val fcmToken: String,
    val generalNewsTopic: Boolean,
    val scholarshipNewsTopic: Boolean,
    val eventNewsTopic: Boolean,
    val academicNewsTopic: Boolean,
    val employmentNewsTopic: Boolean,
    val deviceType: DeviceType,
    val isActive: Boolean,

    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)