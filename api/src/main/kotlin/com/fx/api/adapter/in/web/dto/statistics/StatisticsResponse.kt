package com.fx.api.adapter.`in`.web.dto.statistics

import com.fx.global.domain.DailyStatistics
import java.time.LocalDate

data class StatisticsResponse(
    // ID
    val statisticsDate: LocalDate,

    // 공지
    val noticeCount: Long,
    val noticeSummaryCount: Long,

    // FCM 활성 토큰
    val fcmTokenActiveCount: Long,
    val fcmTokenActiveAosCount: Long,
    val fcmTokenActiveIosCount: Long,

    // FCM 비활성 토큰
    val fcmTokenInactiveCount: Long,
    val fcmTokenInactiveAosCount: Long,
    val fcmTokenInactiveIosCount: Long,

) {

    companion object {
        fun from(dailyStatistics: DailyStatistics): StatisticsResponse =
            StatisticsResponse(
                statisticsDate = dailyStatistics.statisticsDate,
                noticeCount = dailyStatistics.noticeCount,
                noticeSummaryCount = dailyStatistics.noticeSummaryCount,
                fcmTokenActiveCount = dailyStatistics.fcmTokenActiveCount,
                fcmTokenActiveAosCount = dailyStatistics.fcmTokenActiveAosCount,
                fcmTokenActiveIosCount = dailyStatistics.fcmTokenActiveIosCount,
                fcmTokenInactiveCount = dailyStatistics.fcmTokenInactiveCount,
                fcmTokenInactiveAosCount = dailyStatistics.fcmTokenInactiveAosCount,
                fcmTokenInactiveIosCount = dailyStatistics.fcmTokenInactiveIosCount
            )

        fun from(dailyStatisticsList: List<DailyStatistics>): List<StatisticsResponse> =
            dailyStatisticsList.map { from(it) }

    }

}