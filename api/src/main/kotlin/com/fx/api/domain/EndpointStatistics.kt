package com.fx.api.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

/** 엔드포인트(메서드 + URL 패턴) 하나의 기간 합계 */
data class EndpointStatistics(
    val method: String,
    /** 요청이 매핑된 URL 패턴 (예: `/open-api/v1/notices/{nttId}`) */
    val urlPattern: String,
    val counts: RequestCounts,
)

/** 엔드포인트 하나의 하루치 통계. 집계된 날에 요청이 없었으면 모두 0 이다. */
data class DailyEndpointStatistics(
    val statisticsDate: LocalDate,
    val counts: RequestCounts,
    /** 그날 이 엔드포인트를 부른 기기 수 */
    val deviceCount: Int,
)

/** 요청 · 오류 수와 처리 시간. 여러 날을 더할 수 있도록 평균 대신 합계를 담는다. */
data class RequestCounts(
    val requestCount: Long,
    /** 4xx 응답 수 */
    val clientErrorCount: Long,
    /** 5xx 응답 수 */
    val serverErrorCount: Long,
    val totalDurationMs: Long,
    /** 요청이 없으면 null */
    val maxDurationMs: Int?,
) {

    /** 처리 시간 합계 ÷ 요청 수를 소수 첫째 자리까지 반올림한다. 요청이 없으면 null */
    val averageDurationMs: Double?
        get() =
            if (requestCount == 0L) {
                null
            } else {
                BigDecimal.valueOf(totalDurationMs)
                    .divide(BigDecimal.valueOf(requestCount), 1, RoundingMode.HALF_UP)
                    .toDouble()
            }

}
