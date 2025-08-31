package com.fx.crawler.appllication.port.`in`

import com.fx.global.domain.Notice

interface NotificationUseCase {

    suspend fun sendNotification(notices: List<Notice>)

}