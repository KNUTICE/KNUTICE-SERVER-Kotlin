package com.fx.api.adapter.`in`.web.dto.topic

import com.fx.api.application.port.`in`.dto.TopicUpdateCommand
import com.fx.global.domain.TopicType
import com.fx.api.exception.TopicException
import com.fx.api.exception.errorcode.TopicErrorCode
import com.fx.global.domain.CrawlableType
import com.fx.global.domain.MajorType
import com.fx.global.domain.MealType
import com.fx.global.domain.NoticeType

data class TopicUpdateRequest (

    val topic: String,
    val enabled: Boolean

) {

    fun toCommand(fcmToken: String, topicType: TopicType): TopicUpdateCommand {
        val enumValue: CrawlableType = try {
            when (topicType) {
                TopicType.NOTICE -> NoticeType.valueOf(topic)
                TopicType.MAJOR -> MajorType.valueOf(topic)
                TopicType.MEAL -> MealType.valueOf(topic)
            }
        } catch (e: IllegalArgumentException) {
            throw TopicException(TopicErrorCode.TOPIC_NOT_FOUND)
        }
        return TopicUpdateCommand(
            fcmToken = fcmToken,
            topicType = topicType,
            topic = enumValue,
            enabled = enabled
        )
    }

}
