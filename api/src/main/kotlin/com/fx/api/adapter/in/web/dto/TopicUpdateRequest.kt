package com.fx.api.adapter.`in`.web.dto

import com.fx.api.application.port.`in`.dto.TopicUpdateCommand

data class TopicUpdateRequest(

    val fcmToken: String,
    val subscribedTopics: Set<String>

) {
    fun from(): TopicUpdateCommand =
        TopicUpdateCommand(
            fcmToken = this.fcmToken,
            subscribedTopics = subscribedTopics
        )
}