package com.fx.api.application.port.`in`

import com.fx.api.domain.TopicCategory
import com.fx.global.domain.FcmToken

interface FcmTokenQueryUseCase {

    fun getMyTopics(fcmToken: String, category: TopicCategory): FcmToken

}