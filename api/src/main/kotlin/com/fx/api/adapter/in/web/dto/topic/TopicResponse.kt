package com.fx.api.adapter.`in`.web.dto.topic

import com.fx.api.domain.TopicCategory
import com.fx.global.domain.FcmToken
import com.fx.global.domain.MealType

data class TopicResponse(
    val subscribedTopics: Set<String>
) {
    companion object {
        fun from(fcmToken: FcmToken, category: TopicCategory): TopicResponse {
            val topics = when(category) {
                TopicCategory.NOTICE -> fcmToken.subscribedNoticeTopics
                TopicCategory.MAJOR -> fcmToken.subscribedMajorTopics
                TopicCategory.MEAL -> fcmToken.subscribedMealTopics
            }
            return TopicResponse(subscribedTopics = topics.map { it.name }.toSet())
        }
    }
}