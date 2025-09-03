package com.fx.api.application.port.`in`

import com.fx.api.application.port.`in`.dto.FcmTokenSaveCommand
import com.fx.api.application.port.`in`.dto.TopicUpdateCommand
import com.fx.global.domain.FcmToken

interface FcmTokenCommandUseCase {

    fun saveFcmToken(fcmTokenSaveCommand: FcmTokenSaveCommand): Boolean
    fun updateTopics(topicUpdateCommand: TopicUpdateCommand): FcmToken

}