package com.fx.api.application.port.`in`.fcmtoken

import com.fx.api.application.port.`in`.fcmtoken.dto.FcmTokenLanguageUpdateCommand
import com.fx.api.application.port.`in`.fcmtoken.dto.FcmTokenSaveCommand
import com.fx.api.application.port.`in`.fcmtoken.dto.FcmTokenUpdateCommand
import com.fx.api.application.port.`in`.fcmtoken.dto.TopicUpdateCommand

interface FcmTokenCommandUseCase {

    fun saveFcmToken(fcmTokenSaveCommand: FcmTokenSaveCommand): Boolean
    fun updateFcmToken(fcmTokenUpdateCommand: FcmTokenUpdateCommand): Boolean
    fun updateTopic(topicUpdateCommand: TopicUpdateCommand): Boolean

    /** 알림 언어를 바꾼다. 이후 알림은 이 언어 문구로 보낸다. */
    fun updateLanguage(fcmTokenLanguageUpdateCommand: FcmTokenLanguageUpdateCommand): Boolean

}