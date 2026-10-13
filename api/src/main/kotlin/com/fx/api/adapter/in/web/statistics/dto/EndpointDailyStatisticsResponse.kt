package com.fx.api.adapter.`in`.web.statistics.dto

import com.fx.api.domain.DailyEndpointStatistics
import com.fx.api.domain.PeriodStatistics
import java.time.LocalDate

data class EndpointDailyStatisticsResponse(
    /** 실제로 적용한 조회 기간 (양 끝 포함) */
    val from: LocalDate,
    val to: LocalDate,
    val method: String,
    val urlPattern: String,
    /** 집계된 날만 날짜순. 요청이 없던 날은 0 */
    val days: List<DayResponse>,
) {

    data class DayResponse(
        val statisticsDate: LocalDate,
        val requestCount: Long,
        val clientErrorCount: Long,
        val serverErrorCount: Long,
        /** 요청이 없으면 null */
        val averageDurationMs: Double?,
        /** 요청이 없으면 null */
        val maxDurationMs: Int?,
        /** 그날 이 엔드포인트를 부른 기기 수 */
        val deviceCount: Int,
    ) {

        companion object {

            fun from(statistics: DailyEndpointStatistics): DayResponse =
                DayResponse(
                    statisticsDate = statistics.statisticsDate,
                    requestCount = statistics.counts.requestCount,
                    clientErrorCount = statistics.counts.clientErrorCount,
                    serverErrorCount = statistics.counts.serverErrorCount,
                    averageDurationMs = statistics.counts.averageDurationMs,
                    maxDurationMs = statistics.counts.maxDurationMs,
                    deviceCount = statistics.deviceCount,
                )

        }

    }

    companion object {

        fun from(
            method: String,
            urlPattern: String,
            statistics: PeriodStatistics<DailyEndpointStatistics>,
        ): EndpointDailyStatisticsResponse =
            EndpointDailyStatisticsResponse(
                from = statistics.period.from,
                to = statistics.period.to,
                method = method,
                urlPattern = urlPattern,
                days = statistics.items.map {
                    DayResponse.from(it)
                },
            )

    }

}
