package com.fx.api.adapter.`in`.web.dto.topic

data class TopicResponse(
    val subscribedTopics: Set<String>
) {
    companion object {
        fun from(subscribedTopics: Set<String>): TopicResponse =
            TopicResponse(subscribedTopics)

    }
}