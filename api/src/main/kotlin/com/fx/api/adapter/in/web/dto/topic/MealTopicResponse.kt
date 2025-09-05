package com.fx.api.adapter.`in`.web.dto.topic

import com.fx.global.domain.FcmToken
import com.fx.global.domain.MajorType
import com.fx.global.domain.MealType
import com.fx.global.domain.NoticeType

data class MealTopicResponse(
    val subscribedTopics: Set<MealType>
) {
    companion object {
        fun from(fcmToken: FcmToken): MealTopicResponse =
            MealTopicResponse(
                subscribedTopics = fcmToken.subscribedMealTopics
            )
    }
}