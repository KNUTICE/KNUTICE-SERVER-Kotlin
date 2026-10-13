package com.fx.api.adapter.`in`.web.statistics.dto

import com.fx.api.domain.DailyTopicStatistics
import com.fx.api.domain.PeriodStatistics
import java.time.LocalDate

data class TopicDailyStatisticsResponse(
    /** 실제로 적용한 조회 기간 (양 끝 포함) */
    val from: LocalDate,
    val to: LocalDate,
    val topicCode: Int,
    /** 집계된 날만 날짜순. 조회가 없던 날은 0 */
    val days: List<DayResponse>,
) {

    data class DayResponse(
        val statisticsDate: LocalDate,
        /** 공지 목록 조회 수 (v1 + v2) */
        val requestCount: Long,
    ) {

        companion object {

            fun from(statistics: DailyTopicStatistics): DayResponse =
                DayResponse(
                    statisticsDate = statistics.statisticsDate,
                    requestCount = statistics.requestCount,
                )

        }

    }

    companion object {

        fun from(topicCode: Int, statistics: PeriodStatistics<DailyTopicStatistics>): TopicDailyStatisticsResponse =
            TopicDailyStatisticsResponse(
                from = statistics.period.from,
                to = statistics.period.to,
                topicCode = topicCode,
                days = statistics.items.map {
                    DayResponse.from(it)
                },
            )

    }

}
