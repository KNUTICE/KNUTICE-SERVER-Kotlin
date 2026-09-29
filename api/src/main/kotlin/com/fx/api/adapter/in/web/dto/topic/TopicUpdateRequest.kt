package com.fx.api.adapter.`in`.web.dto.topic

import com.fx.api.application.port.`in`.dto.TopicUpdateCommand
import com.fx.common.domain.TopicType

/** v1 구독 변경. [topic] 은 토픽 이름이며 요청의 토픽 유형에 속해야 한다. */
data class TopicUpdateRequest (
    val topic: String,
    val enabled: Boolean
) {

    fun toCommand(fcmToken: String, topicType: TopicType): TopicUpdateCommand =
        TopicUpdateCommand(
            fcmToken = fcmToken,
            topicType = topicType,
            topicName = topic,
            enabled = enabled
        )

}
