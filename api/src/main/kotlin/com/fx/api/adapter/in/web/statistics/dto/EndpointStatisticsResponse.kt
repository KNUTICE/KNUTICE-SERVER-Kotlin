package com.fx.api.adapter.`in`.web.statistics.dto

import com.fx.api.domain.EndpointStatistics
import com.fx.api.domain.PeriodStatistics
import java.time.LocalDate

data class EndpointStatisticsResponse(
    /** 실제로 적용한 조회 기간 (양 끝 포함) */
    val from: LocalDate,
    val to: LocalDate,
    /** 요청 수 내림차순. 기간 안에 호출되지 않은 엔드포인트는 없다 */
    val endpoints: List<EndpointResponse>,
) {

    data class EndpointResponse(
        val method: String,
        /** 요청이 매핑된 URL 패턴 (예: /open-api/v1/notices/{nttId}) */
        val urlPattern: String,
        val requestCount: Long,
        /** 4xx 응답 수 */
        val clientErrorCount: Long,
        /** 5xx 응답 수 */
        val serverErrorCount: Long,
        /** 기간 평균 처리 시간 (처리 시간 합계 ÷ 요청 수, 소수 첫째 자리) */
        val averageDurationMs: Double?,
        val maxDurationMs: Int?,
    ) {

        companion object {

            fun from(statistics: EndpointStatistics): EndpointResponse =
                EndpointResponse(
                    method = statistics.method,
                    urlPattern = statistics.urlPattern,
                    requestCount = statistics.counts.requestCount,
                    clientErrorCount = statistics.counts.clientErrorCount,
                    serverErrorCount = statistics.counts.serverErrorCount,
                    averageDurationMs = statistics.counts.averageDurationMs,
                    maxDurationMs = statistics.counts.maxDurationMs,
                )

        }

    }

    companion object {

        fun from(statistics: PeriodStatistics<EndpointStatistics>): EndpointStatisticsResponse =
            EndpointStatisticsResponse(
                from = statistics.period.from,
                to = statistics.period.to,
                endpoints = statistics.items.map {
                    EndpointResponse.from(it)
                },
            )

    }

}
