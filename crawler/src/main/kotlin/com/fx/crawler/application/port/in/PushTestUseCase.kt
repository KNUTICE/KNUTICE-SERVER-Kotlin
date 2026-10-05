package com.fx.crawler.application.port.`in`

/** 관리자의 테스트 발송. 토큰 하나에 실제와 같은 알림을 보낸다. */
interface PushTestUseCase {

    fun sendNotice(fcmToken: String, nttId: Long)

    /** @param mealTopicName 학식 토픽 이름 (예: `STUDENT_CAFETERIA`) */
    fun sendMeal(fcmToken: String, mealTopicName: String)

}
