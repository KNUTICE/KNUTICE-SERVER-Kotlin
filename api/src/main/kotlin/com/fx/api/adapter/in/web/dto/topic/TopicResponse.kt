package com.fx.api.adapter.`in`.web.dto.topic

import com.fx.global.domain.TopicType
import com.fx.global.domain.FcmToken

data class TopicResponse(
    val subscribedTopics: Set<String>
) {
    companion object {
        fun from(fcmToken: FcmToken, category: TopicType): TopicResponse {
            val topics = when(category) {
                TopicType.NOTICE -> fcmToken.subscribedNoticeTopics
                TopicType.MAJOR -> fcmToken.subscribedMajorTopics
                TopicType.MEAL -> fcmToken.subscribedMealTopics
            }
            return TopicResponse(subscribedTopics = topics.map { it.name }.toSet())
        }
    }
}