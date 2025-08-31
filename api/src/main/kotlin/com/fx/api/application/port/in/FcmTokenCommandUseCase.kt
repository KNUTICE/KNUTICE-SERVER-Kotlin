package com.fx.api.application.port.`in`

import com.fx.api.application.port.`in`.dto.FcmTokenCommand
import com.fx.api.application.port.`in`.dto.TopicUpdateCommand
import com.fx.global.domain.FcmToken

interface FcmTokenCommandUseCase {

    fun saveFcmToken(fcmTokenCommand: FcmTokenCommand): Boolean
    fun updateTopics(topicUpdateCommand: TopicUpdateCommand): FcmToken

}