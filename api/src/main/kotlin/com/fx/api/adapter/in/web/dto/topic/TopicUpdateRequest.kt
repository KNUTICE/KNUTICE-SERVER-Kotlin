package com.fx.api.adapter.`in`.web.dto.topic

import com.fx.api.application.port.`in`.dto.TopicUpdateCommand

data class TopicUpdateRequest(

    val subscribedTopics: Set<String>

) {
    fun toCommand(fcmToken: String): TopicUpdateCommand =
        TopicUpdateCommand(
            fcmToken = fcmToken,
            subscribedTopics = subscribedTopics
        )
}