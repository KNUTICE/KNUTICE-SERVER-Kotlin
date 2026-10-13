package com.fx.api.adapter.`in`.web.statistics.dto

import com.fx.api.domain.DailyStatistics
import com.fx.api.domain.PeriodStatistics
import java.time.LocalDate
import java.time.LocalDateTime

data class DailyStatisticsResponse(
    /** 실제로 적용한 조회 기간 (양 끝 포함) */
    val from: LocalDate,
    val to: LocalDate,
    /** 집계된 날만 날짜순 */
    val days: List<DayResponse>,
) {

    data class DayResponse(
        val statisticsDate: LocalDate,
        val requestCount: Long,
        /** 기기 종류를 모르는 토큰도 세므로 iOS + Android 보다 클 수 있다 */
        val deviceCount: Int,
        val iosDeviceCount: Int,
        val aosDeviceCount: Int,
        /** 그날 저장된 공지 수 */
        val noticeCount: Int,
        val summaryCompletedCount: Int,
        val summaryFailedCount: Int,
        val newTokenCount: Int,
        /** 이 날짜를 마지막으로 집계한 시각 */
        val aggregatedAt: LocalDateTime,
    ) {

        companion object {

            fun from(statistics: DailyStatistics): DayResponse =
                DayResponse(
                    statisticsDate = statistics.statisticsDate,
                    requestCount = statistics.requestCount,
                    deviceCount = statistics.deviceCount,
                    iosDeviceCount = statistics.iosDeviceCount,
                    aosDeviceCount = statistics.aosDeviceCount,
                    noticeCount = statistics.noticeCount,
                    summaryCompletedCount = statistics.summaryCompletedCount,
                    summaryFailedCount = statistics.summaryFailedCount,
                    newTokenCount = statistics.newTokenCount,
                    aggregatedAt = statistics.aggregatedAt,
                )

        }

    }

    companion object {

        fun from(statistics: PeriodStatistics<DailyStatistics>): DailyStatisticsResponse =
            DailyStatisticsResponse(
                from = statistics.period.from,
                to = statistics.period.to,
                days = statistics.items.map {
                    DayResponse.from(it)
                },
            )

    }

}
