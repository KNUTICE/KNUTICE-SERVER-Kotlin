package com.fx.api.application.port.`in`.dto

import com.fx.common.domain.TopicType

/**
 * 토픽 구독 변경. v1 은 [topicName] (반드시 [topicType] 소속), v2 는 [topicId] 로 지정한다.
 * @property enabled true 면 구독, false 면 해제
 */
data class TopicUpdateCommand(
    val fcmToken: String,
    val topicType: TopicType,
    val topicName: String? = null,
    val topicId: Int? = null,
    val enabled: Boolean,
)
