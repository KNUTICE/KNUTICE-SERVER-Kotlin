package com.fx.api.application.port.`in`

import com.fx.api.application.port.`in`.dto.NoticeNotificationCommand

interface NotificationUseCase {

    fun sendTestNotice(noticeNotificationCommand: NoticeNotificationCommand): Boolean

}