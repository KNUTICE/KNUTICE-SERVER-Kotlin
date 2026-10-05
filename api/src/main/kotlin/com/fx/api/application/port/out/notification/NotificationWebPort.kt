package com.fx.api.application.port.out.notification

interface NotificationWebPort {

    fun notifyNotice(fcmToken: String, nttId: Long): Boolean

    fun notifyMeal(fcmToken: String, mealTopicName: String): Boolean

}
