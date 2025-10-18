package com.fx.api.adapter.`in`.web.dto.notice

import com.fx.api.application.port.`in`.dto.NoticeCommand
import com.fx.api.exception.TopicException
import com.fx.api.exception.errorcode.TopicErrorCode
import com.fx.global.domain.CrawlableType
import com.fx.global.domain.MajorType
import com.fx.global.domain.MealType
import com.fx.global.domain.NoticeType
import com.fx.global.domain.TopicType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.URL
import java.time.LocalDate

data class NoticeRequest(

    @field:NotNull
    val nttId: Long,

    @field:NotBlank
    val title: String,

    @field:NotBlank
    @field:URL
    val contentUrl: String,

    val contentSummary: String? = null,

    val contentImageUrl: String? = null,

    @field:NotBlank
    val department: String,

    @field:NotNull
    val registrationDate: LocalDate,

    @field:NotNull
    val isAttachment: Boolean,

    @field:NotBlank
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