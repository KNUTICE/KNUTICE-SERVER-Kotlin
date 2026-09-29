package com.fx.crawler.appllication.port.`in`

import com.fx.common.domain.Notice
import com.fx.crawler.legacy.readingroom.SeatAlert

interface NotificationUseCase {

    suspend fun sendNotification(notices: List<Notice>)

    suspend fun sendNotification(fcmToken: String, nttId: Long)

    suspend fun sendSilentPushNotification()

    suspend fun sendSeatAlert(alert: SeatAlert)

}