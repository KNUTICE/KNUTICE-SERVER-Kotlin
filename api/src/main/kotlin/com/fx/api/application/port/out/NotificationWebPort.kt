package com.fx.api.application.port.out

import com.fx.global.domain.MealType

interface NotificationWebPort {

    fun notifyNotice(fcmToken: String, nttId: Long): Boolean

    fun notifyMeal(fcmToken: String, mealType: MealType): Boolean

}