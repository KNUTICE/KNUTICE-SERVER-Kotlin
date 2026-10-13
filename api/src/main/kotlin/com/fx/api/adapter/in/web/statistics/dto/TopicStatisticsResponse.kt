package com.fx.api.adapter.`in`.web.statistics.dto

import com.fx.api.domain.PeriodStatistics
import com.fx.api.domain.TopicViewStatistics
import com.fx.common.domain.TopicType
import com.fx.common.domain.i18n.Language
import java.time.LocalDate

data class TopicStatisticsResponse(
    /** 실제로 적용한 조회 기간 (양 끝 포함) */
    val from: LocalDate,
    val to: LocalDate,
    /** 조회 수 내림차순, 같으면 토픽 코드 순 */
    val topics: List<TopicResponse>,
) {

    data class TopicResponse(
        /** 토픽 코드 (v2 topicId) */
        val topicCode: Int,
        /** 토픽 이름 (v1 topic) */
        val topicName: String,
        /** 삭제된 토픽이면 null */
        val topicType: TopicType?,
        /** 한국어 표시명. 삭제된 토픽이면 null */
        val displayName: String?,
        /** 학과 토픽의 단과대 한국어 표시명. 그 밖의 토픽 · 삭제된 토픽은 null */
        val college: String?,
        val deleted: Boolean,
        /** 공지 목록 조회 수 (v1 + v2) */
        val requestCount: Long,
    ) {

        companion object {

            fun from(statistics: TopicViewStatistics): TopicResponse =
                TopicResponse(
                    topicCode = statistics.topicCode,
                    topicName = statistics.topicName,
                    topicType = statistics.topic?.topicType,
                    displayName = statistics.topic?.displayName?.resolve(Language.KO),
                    college = statistics.topic?.college?.displayName?.resolve(Language.KO),
                    deleted = statistics.topic == null,
                    requestCount = statistics.requestCount,
                )

        }

    }

    companion object {

        fun from(statistics: PeriodStatistics<TopicViewStatistics>): TopicStatisticsResponse =
            TopicStatisticsResponse(
                from = statistics.period.from,
                to = statistics.period.to,
                topics = statistics.items.map {
                    TopicResponse.from(it)
                },
            )

    }

}
