package com.fx.crawler.domain.statistics

/** 날짜 하나의 토픽별 공지 목록 조회 수. 목록이 비면 404 로 응답하므로 응답 상태와 상관없이 센다. */
data class TopicStatistics(
    val topicCode: Int,
    val topicName: String,
    val requestCount: Long,
)
