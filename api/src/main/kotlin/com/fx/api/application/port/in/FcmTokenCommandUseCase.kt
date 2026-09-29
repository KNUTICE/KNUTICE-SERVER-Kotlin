package com.fx.api.application.port.`in`

import com.fx.api.application.port.`in`.dto.FcmTokenSaveCommand
import com.fx.api.application.port.`in`.dto.FcmTokenUpdateCommand
import com.fx.api.application.port.`in`.dto.TopicUpdateCommand

interface FcmTokenCommandUseCase {

    fun saveFcmToken(fcmTokenSaveCommand: FcmTokenSaveCommand): Boolean
    fun updateFcmToken(fcmTokenUpdateCommand: FcmTokenUpdateCommand): Boolean
    fun updateTopic(topicUpdateCommand: TopicUpdateCommand): Boolean

}