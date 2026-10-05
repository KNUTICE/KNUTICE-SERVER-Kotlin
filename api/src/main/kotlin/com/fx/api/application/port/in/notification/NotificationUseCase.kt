package com.fx.api.application.port.`in`.notification

interface NotificationUseCase {

    fun notifyNotice(fcmToken: String, nttId: Long): Boolean

    /** [mealTopicName] 학식 토픽(예: `STUDENT_CAFETERIA`)의 알림을 보낸다. */
    fun notifyMeal(fcmToken: String, mealTopicName: String): Boolean

}
