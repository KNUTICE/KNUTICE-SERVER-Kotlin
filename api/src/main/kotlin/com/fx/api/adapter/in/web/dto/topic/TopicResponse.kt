package com.fx.api.adapter.`in`.web.dto.topic

import com.fx.global.domain.FcmToken
import com.fx.global.domain.MealType

data class TopicResponse(
    val subscribedTopics: Set<String>
) {
    companion object {

        fun from(fcmToken: FcmToken, typeSelector: (FcmToken) -> Set<Enum<*>>): TopicResponse {
            val topics = typeSelector(fcmToken).map { it.name }.toSet()
            return TopicResponse(subscribedTopics = topics)
        }

    }
}