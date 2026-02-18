package com.fx.crawler.appllication.port.out

import com.fx.global.domain.FcmToken
import com.fx.global.domain.Meal
import com.fx.global.domain.Notice
import com.fx.readingroom.domain.SeatAlert

interface FcmNotificationPort {

    /**
     * @param target token
     * @param notice type
     * @param message
     * @return failedTokens
     */
    suspend fun sendNotification(fcmTokens: List<FcmToken>, notices: List<Notice>): List<FcmToken>

    suspend fun sendNotification(fcmTokens: List<FcmToken>, meal: Meal): List<FcmToken>

    suspend fun sendSilentPushNotification(fcmTokens: List<FcmToken>): List<FcmToken>

    suspend fun sendSeatAlert(alert: SeatAlert)

}