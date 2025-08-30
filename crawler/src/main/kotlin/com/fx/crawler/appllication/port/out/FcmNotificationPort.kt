package com.fx.crawler.appllication.port.out

import com.fx.crawler.domain.FcmToken
import com.fx.crawler.domain.Notice

interface FcmNotificationPort {

    /**
     * @param target token
     * @param notice type
     * @param message
     * @return failedTokens
     */
    suspend fun sendNotification(fcmTokens: List<FcmToken>, notices: List<Notice>): List<FcmToken>

}