package com.fx.api.adapter.`in`.web.notice.dto

import com.fx.api.application.port.`in`.notice.dto.NoticeCommand
import com.fx.common.domain.TopicType
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
    fun toCommand(topicType: TopicType): NoticeCommand =
        NoticeCommand(
            nttId = nttId,
            title = title,
            contentUrl = contentUrl,
            contentSummary = contentSummary,
            contentImageUrl = contentImageUrl,
            department = department,
            registrationDate = registrationDate,
            isAttachment = isAttachment,
            topicName = topic,
            topicType = topicType
        )

}
