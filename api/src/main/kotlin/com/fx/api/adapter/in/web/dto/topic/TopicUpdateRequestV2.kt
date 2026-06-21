package com.fx.api.adapter.`in`.web.dto.topic

import com.fx.api.application.port.`in`.dto.TopicUpdateCommand
import com.fx.global.domain.CrawlableType
import com.fx.global.domain.TopicType

data class TopicUpdateRequestV2(

    val topicId: Int,
    val enabled: Boolean

) {

    fun toCommand(fcmToken: String, topicType: TopicType): TopicUpdateCommand {
        val enumValue: CrawlableType = CrawlableType.fromCode(topicId)
        return TopicUpdateCommand(
            fcmToken = fcmToken,
            topicType = topicType,
            topic = enumValue,
            enabled = enabled
        )
    }

}
