package com.fx.api.adapter.`in`.web.dto.notification

import com.fx.api.application.port.`in`.dto.NoticeNotificationCommand
import com.fx.global.domain.TopicType
import com.fx.api.exception.TopicException
import com.fx.api.exception.errorcode.TopicErrorCode
import com.fx.global.domain.CrawlableType
import com.fx.global.domain.MajorType
import com.fx.global.domain.NoticeType
import java.time.LocalDate

data class NoticeNotificationRequest(
    val nttId: Long,
    val title: String,
    val department: String,
    val contentUrl: String,
    val contentImageUrl: String? = null,
    val registrationDate: LocalDate,
    val isAttachment: Boolean,
    val topic: String
) {

    fun toCommand(fcmToken: String, topicType: TopicType): NoticeNotificationCommand {
        // TODO : Utils
        try {
            when (topicType) {
                TopicType.NOTICE -> NoticeType.valueOf(this.topic)
                TopicType.MAJOR -> MajorType.valueOf(this.topic)
                else -> {
                    throw TopicException(TopicErrorCode.TOPIC_NOT_FOUND)
                }
            }
        } catch (e: IllegalArgumentException) {
            throw TopicException(TopicErrorCode.TOPIC_NOT_FOUND)
        }
        return NoticeNotificationCommand(
            fcmToken = fcmToken,
            nttId = this.nttId,
            title = this.title,
            department = this.department,
            contentUrl = this.contentUrl,
            contentImageUrl = this.contentImageUrl,
            registrationDate = this.registrationDate,
            isAttachment = this.isAttachment,
            topicType = topicType,
            topic = topic,
        )
    }

}