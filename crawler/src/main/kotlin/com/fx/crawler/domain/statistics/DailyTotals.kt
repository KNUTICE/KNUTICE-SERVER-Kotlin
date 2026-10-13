package com.fx.crawler.domain.statistics

/**
 * 날짜 하나의 전체 API 집계와 그날 생긴 공지 · 토큰 수.
 * 모두 시각 컬럼으로 그날을 골라 세므로 언제 다시 집계해도 같은 값이다 (요약 상태만 집계 시점 값).
 */
data class DailyTotals(
    val requestCount: Long,
    val deviceCount: Int,
    val iosDeviceCount: Int,
    val aosDeviceCount: Int,
    val noticeCount: Int,
    val summaryCompletedCount: Int,
    val summaryFailedCount: Int,
    val newTokenCount: Int,
)
