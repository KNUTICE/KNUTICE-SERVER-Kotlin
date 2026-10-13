package com.fx.api.adapter.`in`.web.statistics.dto

import com.fx.api.domain.DailyTokenStatistics
import com.fx.api.domain.DeviceCounts
import com.fx.api.domain.PeriodStatistics
import java.time.LocalDate

data class TokenStatisticsResponse(
    /** 실제로 적용한 조회 기간 (양 끝 포함) */
    val from: LocalDate,
    val to: LocalDate,
    /** 집계된 날만 날짜순 */
    val days: List<DayResponse>,
) {

    data class DayResponse(
        val statisticsDate: LocalDate,
        val newTokenCount: Int,
        /** 그날이 끝난 직후의 활성 토큰. 일일 통계 Job 이 그날 현황을 기록하지 못했으면 null */
        val active: TokenCountResponse?,
        /** 같은 시점의 비활성 토큰. 기록하지 못했으면 null */
        val inactive: TokenCountResponse?,
    ) {

        companion object {

            fun from(statistics: DailyTokenStatistics): DayResponse =
                DayResponse(
                    statisticsDate = statistics.statisticsDate,
                    newTokenCount = statistics.newTokenCount,
                    active = statistics.active?.let {
                        TokenCountResponse.from(it)
                    },
                    inactive = statistics.inactive?.let {
                        TokenCountResponse.from(it)
                    },
                )

        }

    }

    data class TokenCountResponse(
        val ios: Long,
        val aos: Long,
        /** iOS + Android */
        val total: Long,
    ) {

        companion object {

            fun from(counts: DeviceCounts): TokenCountResponse =
                TokenCountResponse(
                    ios = counts.ios,
                    aos = counts.aos,
                    total = counts.total,
                )

        }

    }

    companion object {

        fun from(statistics: PeriodStatistics<DailyTokenStatistics>): TokenStatisticsResponse =
            TokenStatisticsResponse(
                from = statistics.period.from,
                to = statistics.period.to,
                days = statistics.items.map {
                    DayResponse.from(it)
                },
            )

    }

}
