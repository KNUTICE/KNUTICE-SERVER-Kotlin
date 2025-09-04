package com.fx.crawler.domain

import com.fx.global.domain.DeviceType
import com.fx.global.domain.MajorType
import com.fx.global.domain.NoticeType
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

data class FcmTokenQuery(
    val createdAt: LocalDateTime? = null,
    val isActive: Boolean,
    val subscribedNoticeTopic: NoticeType? = null,
    val subscribedMajorTopic: MajorType? = null,
    val deviceType: DeviceType? = null,
    val pageable: Pageable,
)
