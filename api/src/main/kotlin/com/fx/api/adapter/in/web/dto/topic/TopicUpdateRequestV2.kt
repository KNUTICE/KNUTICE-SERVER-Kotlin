package com.fx.api.adapter.`in`.web.dto.topic

import com.fx.api.application.port.`in`.dto.TopicUpdateCommand
import com.fx.common.domain.TopicType

/** v2 구독 변경. [topicId] 는 토픽 코드. */
data class TopicUpdateRequestV2(
    val topicId: Int,
    val enabled: Boolean
) {

    fun toCommand(fcmToken: String, topicType: TopicType): TopicUpdateCommand =
        TopicUpdateCommand(
            fcmToken = fcmToken,
            topicType = topicType,
            topicId = topicId,
            enabled = enabled
        )

}
