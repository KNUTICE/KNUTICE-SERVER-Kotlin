package com.fx.crawler.appllication.port.`in`

import com.fx.crawler.domain.Notice

interface NotificationUseCase {

    suspend fun sendNotification(notices: List<Notice>)

}