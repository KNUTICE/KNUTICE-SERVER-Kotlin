package com.fx.api.application.service

import com.fx.api.application.port.`in`.NotificationUseCase
import com.fx.api.application.port.`in`.dto.NoticeNotificationCommand
import com.fx.api.application.port.out.NotificationWebPort
import com.fx.api.domain.PushNotice
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val notificationWebPort: NotificationWebPort
) : NotificationUseCase {

    override fun pushTestNotice(noticeNotificationCommand: NoticeNotificationCommand): Boolean =
        notificationWebPort.pushTestNotice(PushNotice.create(noticeNotificationCommand))

}