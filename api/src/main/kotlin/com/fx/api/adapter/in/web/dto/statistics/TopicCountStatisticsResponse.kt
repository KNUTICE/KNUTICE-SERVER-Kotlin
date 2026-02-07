package com.fx.api.adapter.`in`.web.dto.statistics

import com.fx.api.domain.DailyTopicCount
import com.fx.global.domain.DailyApiLogStatistics
import java.time.LocalDate

data class TopicCountStatisticsResponse(
    val date: LocalDate,
    val topic: String,
    val count: Long
) {
    companion object {
        fun from(list: List<DailyTopicCount>) =
            list.map {
                TopicCountStatisticsResponse(
                    date = it.date,
                    topic = it.topic,
                    count = it.count
                )
            }
    }
}