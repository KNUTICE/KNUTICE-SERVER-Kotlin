package com.fx.global.domain

import java.time.LocalDate
import java.time.LocalDateTime

data class DailyApiLogStatistics(

    // ID (형식: "2026-02-07_/open-api/v1/notices_GET")
    val id: String? = null,

    // 통계 날짜
    val statisticsDate: LocalDate,

    // API 식별 정보
    val urlPattern: String,
    val method: String,

    // 통계 수치
    val totalCount: Long,
    val errorCount: Long,
    val averageExecutionTime: Double,

    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null

)