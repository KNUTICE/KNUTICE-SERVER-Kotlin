package com.fx.api.application.port.`in`

import com.fx.api.application.port.`in`.dto.NoticeNotificationCommand

interface NotificationUseCase {

    fun pushTestNotice(noticeNotificationCommand: NoticeNotificationCommand): Boolean

}