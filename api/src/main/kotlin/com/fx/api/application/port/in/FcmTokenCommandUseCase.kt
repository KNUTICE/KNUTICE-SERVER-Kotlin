package com.fx.api.application.port.`in`

import com.fx.api.application.port.`in`.dto.FcmTokenSaveCommand
import com.fx.api.application.port.`in`.dto.FcmTokenUpdateCommand
import com.fx.api.application.port.`in`.dto.TopicUpdateCommand
import com.fx.global.domain.MajorType
import com.fx.global.domain.MealType
import com.fx.global.domain.NoticeType

interface FcmTokenCommandUseCase {

    fun saveFcmToken(fcmTokenSaveCommand: FcmTokenSaveCommand): Boolean
    fun updateFcmToken(fcmTokenUpdateCommand: FcmTokenUpdateCommand): Boolean
    fun updateTopic(topicUpdateCommand: TopicUpdateCommand): Boolean

}