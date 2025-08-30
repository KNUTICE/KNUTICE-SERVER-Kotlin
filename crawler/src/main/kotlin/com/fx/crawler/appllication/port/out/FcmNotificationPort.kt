package com.fx.crawler.appllication.port.out

import com.fx.crawler.domain.CrawlableType
import com.fx.crawler.domain.FcmToken

interface FcmNotificationPort {

    /**
     * @param target token
     * @param notice type
     * @param message
     * @return failedTokens
     */
    suspend fun sendNotification(fcmTokens: List<FcmToken>, notice: CrawlableType, message: String): List<FcmToken>

}