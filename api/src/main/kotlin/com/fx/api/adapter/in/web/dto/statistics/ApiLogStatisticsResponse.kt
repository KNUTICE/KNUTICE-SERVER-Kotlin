package com.fx.api.adapter.`in`.web.dto.statistics

import com.fx.global.domain.DailyApiLogStatistics
import java.time.LocalDate

data class ApiLogStatisticsResponse(
    val id: String?,
    val statisticsDate: LocalDate,

    val urlPattern: String,
    val method: String,

    val totalCount: Long,
    val errorCount: Long,
    val averageExecutionTime: Double
) {

    companion object {
        fun from(dailyApiLogStatistics: DailyApiLogStatistics) =
            ApiLogStatisticsResponse(
                id = dailyApiLogStatistics.id,
                statisticsDate = dailyApiLogStatistics.statisticsDate,
                urlPattern = dailyApiLogStatistics.urlPattern,
                method = dailyApiLogStatistics.method,
                totalCount = dailyApiLogStatistics.totalCount,
                errorCount = dailyApiLogStatistics.errorCount,
                averageExecutionTime = dailyApiLogStatistics.averageExecutionTime
            )

        fun from(list: List<DailyApiLogStatistics>) =
            list.map { from(it) }
    }

}