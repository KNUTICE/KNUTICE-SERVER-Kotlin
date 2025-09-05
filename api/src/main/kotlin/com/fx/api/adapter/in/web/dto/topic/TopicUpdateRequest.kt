package com.fx.api.adapter.`in`.web.dto.topic

import com.fx.api.application.port.`in`.dto.TopicUpdateCommand
import com.fx.api.domain.TopicCategory
import com.fx.api.exception.TopicException
import com.fx.api.exception.errorcode.TopicErrorCode
import com.fx.global.domain.MajorType
import com.fx.global.domain.MealType
import com.fx.global.domain.NoticeType

data class TopicUpdateRequest (

    val topic: String,
    val enabled: Boolean

) {

    fun toCommand(fcmToken: String, category: TopicCategory): TopicUpdateCommand {
        val enumValue: Enum<*> = try {
            when (category) {
                TopicCategory.NOTICE -> NoticeType.valueOf(topic)
                TopicCategory.MAJOR -> MajorType.valueOf(topic)
                TopicCategory.MEAL -> MealType.valueOf(topic)
            }
        } catch (e: IllegalArgumentException) {
            throw TopicException(TopicErrorCode.TOPIC_NOT_FOUND)
        }
        return TopicUpdateCommand(
            fcmToken = fcmToken,
            category = category,
            topic = enumValue,
            enabled = enabled
        )
    }

}
