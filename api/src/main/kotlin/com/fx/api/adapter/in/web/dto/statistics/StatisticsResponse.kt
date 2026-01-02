package com.fx.api.adapter.`in`.web.dto.statistics

import com.fx.api.domain.Statistics

data class StatisticsResponse(
    val noticeCount: Long,
    val noticeSummaryCount: Long,
    val fcmTokenCount: Long,
    val aosActiveFcmTokenCount: Long,
    val iosActiveFcmTokenCount: Long
) {

    companion object {
        fun from(statistics: Statistics): StatisticsResponse =
            StatisticsResponse(
                noticeCount = statistics.noticeCount,
                noticeSummaryCount = statistics.summaryContentCount,
                fcmTokenCount = statistics.fcmTokenCount,
                aosActiveFcmTokenCount = statistics.aosActiveFcmTokenCount,
                iosActiveFcmTokenCount = statistics.iosActiveFcmTokenCount
            )
    }

}