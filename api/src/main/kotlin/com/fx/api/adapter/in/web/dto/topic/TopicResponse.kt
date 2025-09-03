package com.fx.api.adapter.`in`.web.dto.topic

import com.fx.global.domain.FcmToken

data class TopicResponse(
    val subscribedTopics: Set<String>
) {
    companion object {
        fun from(fcmToken: FcmToken): TopicResponse =
            TopicResponse(
                subscribedTopics = fcmToken.subscribedTopics
            )
    }
}