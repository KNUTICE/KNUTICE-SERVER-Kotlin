package com.fx.api.adapter.`in`.web.dto.notice

import com.fx.global.domain.CrawlableType
import com.fx.global.domain.Notice
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
    val topic: CrawlableType

) {

    companion object {

        fun from(notice: Notice): NoticeResponse =
            NoticeResponse(
                nttId = notice.nttId,
                title = notice.title,
                contentUrl = notice.contentUrl,
                contentImageUrl = notice.contentImageUrl,
                isContentSummary = notice.contentSummary != null,
                department = notice.department,
                registrationDate = notice.registrationDate,
                isAttachment = notice.isAttachment,
                topic = notice.topic
            )

        fun from(notices: List<Notice>): List<NoticeResponse> =
            notices.map { this.from(it) }
    }

}
