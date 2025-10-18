package com.fx.api.adapter.`in`.web.dto.notice

import com.fx.api.application.port.`in`.dto.NoticeCommand
import com.fx.api.exception.TopicException
import com.fx.api.exception.errorcode.TopicErrorCode
import com.fx.global.domain.CrawlableType
import com.fx.global.domain.MajorType
import com.fx.global.domain.MealType
import com.fx.global.domain.NoticeType
import com.fx.global.domain.TopicType
import java.time.LocalDate

data class NoticeRequest(

    val nttId: Long,
    val title: String,
    val contentUrl: String,
    val contentSummary: String? = null,
    val contentImageUrl: String? = null,
    val department: String,
    val registrationDate: LocalDate,
    val isAttachment: Boolean,
    val topic: String

) {
    fun toCommand(topicType: TopicType): NoticeCommand {
        // TODO : Utils
        val enumValue: CrawlableType = try {
            when (topicType) {
                TopicType.NOTICE -> NoticeType.valueOf(topic)
                TopicType.MAJOR -> MajorType.valueOf(topic)
                TopicType.MEAL -> MealType.valueOf(topic)
            }
        } catch (e: IllegalArgumentException) {
            throw TopicException(TopicErrorCode.TOPIC_NOT_FOUND)
        }
        return NoticeCommand(
            nttId = nttId,
            title = title,
            contentUrl = contentUrl,
            contentSummary = contentSummary,
            contentImageUrl = contentImageUrl,
            department =department,
            registrationDate = registrationDate,
            isAttachment = isAttachment,
            topic = enumValue
        )
    }
}