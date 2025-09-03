package com.fx.crawler.domain

import com.fx.global.domain.DeviceType
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

data class FcmTokenQuery(
    val createdAt: LocalDateTime? = null,
    val isActive: Boolean,
    val subscribedTopic: String? = null,
    val deviceType: DeviceType? = null,
    val pageable: Pageable,
)
