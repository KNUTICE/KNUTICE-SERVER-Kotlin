package com.fx.api.application.port.`in`

import com.fx.global.domain.FcmToken

interface FcmTokenQueryUseCase {

    fun getMyNoticeTopics(fcmToken: String): FcmToken

    fun getMyMajorTopics(fcmToken: String): FcmToken

    fun getMyMealTopics(fcmToken: String): FcmToken

}