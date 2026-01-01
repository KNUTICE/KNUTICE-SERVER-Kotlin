package com.fx.crawler.appllication.port.`in`

import com.fx.global.domain.Meal

interface MealNotificationUseCase {

    suspend fun sendNotification(meals: List<Meal>)

    /**
     * 특정 사용자에게 학식 알림을 발송합니다.
     */
    suspend fun sendNotification(fcmToken: String, meal: Meal)

}