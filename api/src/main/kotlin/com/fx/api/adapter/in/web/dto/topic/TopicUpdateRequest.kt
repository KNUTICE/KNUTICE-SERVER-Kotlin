package com.fx.api.adapter.`in`.web.dto.topic

import com.fx.api.application.port.`in`.dto.TopicUpdateCommand
import com.fx.global.domain.CrawlableType
import com.fx.global.domain.TopicType
import com.fx.global.utils.TopicUtils

data class TopicUpdateRequest (

    val topic: String,
    val enabled: Boolean

) {

    fun toCommand(fcmToken: String, topicType: TopicType): TopicUpdateCommand {
        val enumValue: CrawlableType = TopicUtils.parseToCrawlable(topic, topicType)
        return TopicUpdateCommand(
            fcmToken = fcmToken,
            topicType = topicType,
            topic = enumValue,
            enabled = enabled
        )
    }

}
