package com.fx.api.application.port.`in`

import com.fx.global.domain.TopicType
import com.fx.global.domain.FcmToken

interface FcmTokenQueryUseCase {

    suspend fun getMyTopics(fcmToken: String, type: TopicType): Set<String>

}