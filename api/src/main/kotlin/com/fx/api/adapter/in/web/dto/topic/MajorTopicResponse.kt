package com.fx.api.adapter.`in`.web.dto.topic

import com.fx.global.domain.FcmToken
import com.fx.global.domain.MajorType
import com.fx.global.domain.NoticeType

data class MajorTopicResponse(
    val subscribedTopics: Set<MajorType>
) {
    companion object {
        fun from(fcmToken: FcmToken): MajorTopicResponse =
            MajorTopicResponse(
                subscribedTopics = fcmToken.subscribedMajorTopics
            )
    }
}