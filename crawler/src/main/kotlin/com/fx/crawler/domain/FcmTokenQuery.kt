package com.fx.crawler.domain

import com.fx.common.domain.DeviceType
import com.fx.common.domain.MajorType
import com.fx.common.domain.MealType
import com.fx.common.domain.NoticeType
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

data class FcmTokenQuery(
    val createdAt: LocalDateTime? = null,
    val isActive: Boolean,
    val subscribedNoticeTopic: NoticeType? = null,
    val subscribedMajorTopic: MajorType? = null,
    val subscribedMealTopic: MealType? = null,
    val deviceType: DeviceType? = null,
    val pageable: Pageable,
)
