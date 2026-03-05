package com.fx.api.application.port.out

import com.fx.global.domain.MealType

interface NotificationRemotePort {

    suspend fun notifyNotice(fcmToken: String, nttId: Long): Boolean

    suspend fun notifyMeal(fcmToken: String, mealType: MealType): Boolean

}