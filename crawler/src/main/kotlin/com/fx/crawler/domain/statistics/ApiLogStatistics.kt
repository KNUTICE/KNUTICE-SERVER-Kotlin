package com.fx.crawler.domain.statistics

/**
 * 날짜 하나의 엔드포인트별 API 집계.
 * 평균 처리 시간은 [totalDurationMs] / [requestCount] 로 구한다 (여러 날짜의 평균도 합끼리 나눠 정확히 구할 수 있도록 합을 둔다).
 * @property deviceCount 요청한 기기 수 (`fcmToken` 헤더가 있던 요청만 센다)
 */
data class ApiLogStatistics(
    val method: String,
    val urlPattern: String,
    val requestCount: Long,
    val clientErrorCount: Long,
    val serverErrorCount: Long,
    val totalDurationMs: Long,
    val maxDurationMs: Int,
    val deviceCount: Int,
)
