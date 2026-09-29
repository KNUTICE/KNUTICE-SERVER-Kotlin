package com.fx.crawler.appllication.port.out

import com.fx.common.domain.FcmToken
import com.fx.common.domain.Meal
import com.fx.common.domain.Notice
import com.fx.crawler.legacy.readingroom.SeatAlert

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