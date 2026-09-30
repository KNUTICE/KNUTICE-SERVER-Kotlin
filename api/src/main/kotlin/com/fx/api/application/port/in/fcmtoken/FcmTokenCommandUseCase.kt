package com.fx.api.application.port.`in`.fcmtoken

import com.fx.api.application.port.`in`.fcmtoken.dto.FcmTokenSaveCommand
import com.fx.api.application.port.`in`.fcmtoken.dto.FcmTokenUpdateCommand
import com.fx.api.application.port.`in`.fcmtoken.dto.TopicUpdateCommand

interface FcmTokenCommandUseCase {

    fun saveFcmToken(fcmTokenSaveCommand: FcmTokenSaveCommand): Boolean
    fun updateFcmToken(fcmTokenUpdateCommand: FcmTokenUpdateCommand): Boolean
    fun updateTopic(topicUpdateCommand: TopicUpdateCommand): Boolean

}