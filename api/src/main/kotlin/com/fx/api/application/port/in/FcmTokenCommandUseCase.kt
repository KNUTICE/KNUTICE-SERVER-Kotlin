package com.fx.api.application.port.`in`

import com.fx.api.application.port.`in`.dto.FcmTokenSaveCommand
import com.fx.api.application.port.`in`.dto.FcmTokenUpdateCommand
import com.fx.api.application.port.`in`.dto.topic.MajorTopicUpdateCommand
import com.fx.api.application.port.`in`.dto.topic.MealTopicUpdateCommand
import com.fx.api.application.port.`in`.dto.topic.NoticeTopicUpdateCommand

interface FcmTokenCommandUseCase {

    fun saveFcmToken(fcmTokenSaveCommand: FcmTokenSaveCommand): Boolean
    fun updateFcmToken(fcmTokenUpdateCommand: FcmTokenUpdateCommand): Boolean
    fun updateNoticeTopic(noticeTopicUpdateCommand: NoticeTopicUpdateCommand): Boolean
    fun updateMajorTopic(majorTopicUpdateCommand: MajorTopicUpdateCommand): Boolean
    fun updateMealTopic(mealTopicUpdateCommand: MealTopicUpdateCommand): Boolean

}