package com.fx.api.adapter.`in`.web.dto

import com.fx.api.application.port.`in`.dto.TopicUpdateCommand
import jakarta.validation.constraints.NotBlank

data class TopicUpdateRequest(

    @field:NotBlank
    val fcmToken: String,


    val subscribedTopics: Set<String>

) {
    fun from(): TopicUpdateCommand =
        TopicUpdateCommand(
            fcmToken = this.fcmToken,
            subscribedTopics = subscribedTopics
        )
}