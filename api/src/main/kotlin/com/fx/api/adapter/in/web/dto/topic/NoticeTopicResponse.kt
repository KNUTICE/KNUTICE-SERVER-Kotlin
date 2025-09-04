package com.fx.api.adapter.`in`.web.dto.topic

import com.fx.global.domain.FcmToken
import com.fx.global.domain.NoticeType

data class NoticeTopicResponse(
    val subscribedTopics: Set<NoticeType>
) {
    companion object {
        fun from(fcmToken: FcmToken): NoticeTopicResponse =
            NoticeTopicResponse(
                subscribedTopics = fcmToken.subscribedNoticeTopics
            )
    }
}