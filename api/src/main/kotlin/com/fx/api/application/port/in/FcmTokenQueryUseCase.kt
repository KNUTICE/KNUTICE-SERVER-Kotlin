package com.fx.api.application.port.`in`

import com.fx.global.domain.TopicType
import com.fx.global.domain.FcmToken

interface FcmTokenQueryUseCase {

    fun getMyTopics(fcmToken: String, type: TopicType): FcmToken

}