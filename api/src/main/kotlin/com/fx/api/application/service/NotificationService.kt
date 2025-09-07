package com.fx.api.application.service

import com.fx.api.application.port.`in`.NotificationUseCase
import com.fx.api.application.port.`in`.dto.NoticeNotificationCommand
import com.fx.api.application.port.out.NotificationWebPort
import com.fx.global.domain.Notice
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val notificationWebPort: NotificationWebPort
) : NotificationUseCase {

    override fun sendTestNotice(noticeNotificationCommand: NoticeNotificationCommand): Boolean =
        notificationWebPort.pushTestNotice(
            noticeNotificationCommand.fcmToken,
            noticeNotificationCommand.topicType,
            createNotice(noticeNotificationCommand)
        )

    private fun createNotice(command: NoticeNotificationCommand): Notice =
        Notice(
            nttId = command.nttId,
            title = command.title,
            department = command.department,
            contentUrl = command.contentUrl,
            contentImageUrl = command.contentImageUrl,
            registrationDate = command.registrationDate,
            isAttachment = command.isAttachment,
            type = command.topic,
        )
}