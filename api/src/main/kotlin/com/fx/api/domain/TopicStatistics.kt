package com.fx.api.domain

import com.fx.common.domain.catalog.TopicView
import java.time.LocalDate
import java.time.LocalDateTime

/** 토픽별 공지 목록 조회 수의 기간 합계 (`daily_topic_statistics`) */
data class TopicRequestCount(
    val topicCode: Int,
    val topicName: String,
    val requestCount: Long,
)

/** 토픽별 공지 목록 조회 수에 카탈로그의 토픽 정보를 붙인 것 */
data class TopicViewStatistics(
    val topicCode: Int,
    val topicName: String,
    /** 카탈로그에 없는(삭제된) 토픽이면 null */
    val topic: TopicView?,
    val requestCount: Long,
)

/** 토픽 하나의 하루치 공지 목록 조회 수. 집계된 날에 조회가 없었으면 0 이다. */
data class DailyTopicStatistics(
    val statisticsDate: LocalDate,
    val requestCount: Long,
)

/** 지금 시점의 토픽별 구독자 수 */
data class TopicSubscriptionStatistics(
    val measuredAt: LocalDateTime,
    /** 활성 토큰 전체. 구독률의 분모 */
    val activeTokens: DeviceCounts,
    /** 카탈로그의 토픽 전체 (code 순) */
    val topics: List<TopicSubscriberCount>,
)

data class TopicSubscriberCount(
    val topic: TopicView,
    /** 이 토픽을 구독한 활성 토큰 */
    val subscribers: DeviceCounts,
)
