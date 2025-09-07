package com.fx.api.domain

import com.fx.api.application.port.`in`.dto.NoticeNotificationCommand
import com.fx.global.domain.TopicType
import java.time.LocalDate

data class PushNotice(

    val fcmToken: String,
    val nttId: Long,
    val title: String,
    val department: String,
    val contentUrl: String,
    val contentImageUrl: String?,
    val registrationDate: LocalDate,
    val isAttachment: Boolean,
    val topicType: TopicType,
    val topic: String

) {

    companion object {

        fun create(notificationCommand: NoticeNotificationCommand): PushNotice =
            PushNotice(
                fcmToken = notificationCommand.fcmToken,
                nttId = notificationCommand.nttId,
                title = notificationCommand.title,
                department = notificationCommand.department,
                contentUrl = notificationCommand.contentUrl,
                contentImageUrl = notificationCommand.contentImageUrl,
                registrationDate = notificationCommand.registrationDate,
                isAttachment = notificationCommand.isAttachment,
                topicType = notificationCommand.topicType,
                topic = notificationCommand.topic
            )

    }

}