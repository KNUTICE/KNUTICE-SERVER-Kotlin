package com.fx.api.application.port.`in`

import com.fx.common.domain.TopicType
import com.fx.common.domain.FcmToken

interface FcmTokenQueryUseCase {

    fun getMyTopics(fcmToken: String, type: TopicType): Set<String>

}