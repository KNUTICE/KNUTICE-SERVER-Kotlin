package com.fx.global.domain

import java.time.LocalDate
import java.time.LocalDateTime

data class DailyStatistics (

    // ID
    val statisticsDate: LocalDate,

    // 공지
    val noticeCount: Long,
    val noticeSummaryCount: Long,

    // FCM 활성 토큰
    val fcmTokenActiveCount: Long,
    val fcmTokenActiveAosCount: Long,
    val fcmTokenActiveIosCount: Long,

    // FCM 비활성 토큰
    val fcmTokenInactiveCount: Long,
    val fcmTokenInactiveAosCount: Long,
    val fcmTokenInactiveIosCount: Long,

    val createdAt: LocalDateTime? = null, // nttId(@Id) 를 미리 지정한 경우 createdAt 생성 불가.
    val updatedAt: LocalDateTime? = null

)

