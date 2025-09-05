package com.fx.crawler.appllication.port.`in`

import com.fx.global.domain.Meal

interface MealNotificationUseCase {

    suspend fun sendNotification(meals: List<Meal>)

}