package com.fx.api.adapter.`in`.web.notice.dto

import com.fx.common.domain.notice.Notice
import java.time.LocalDate

data class NoticeResponse(
    val nttId: Long,
    val title: String,
    val contentUrl: String,
    val contentImageUrl: String? = null,
    val isContentSummary: Boolean,
    val department: String,
    val registrationDate: LocalDate,
    val isAttachment: Boolean,
    /** v1 토픽 이름 (예: `GENERAL_NEWS`) */
    val topic: String
) {

    companion object {

        fun from(notice: Notice): NoticeResponse =
            NoticeResponse(
                nttId = notice.nttId,
                title = notice.title,
                contentUrl = notice.contentUrl,
                contentImageUrl = notice.contentImageUrl,
                isContentSummary = notice.hasSummary,
                department = notice.department,
                registrationDate = notice.registrationDate,
                isAttachment = notice.isAttachment,
                topic = notice.topicName
            )

        fun from(notices: List<Notice>): List<NoticeResponse> =
            notices.map {
                this.from(it)
            }
    }

}
