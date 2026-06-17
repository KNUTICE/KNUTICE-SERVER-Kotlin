package com.fx.api.adapter.`in`.web.dto.notice

import com.fx.global.domain.CrawlableType
import com.fx.global.domain.Notice
import java.time.LocalDate

data class NoticeResponseV2(

    val nttId: Long,
    val title: String,
    val contentUrl: String,
    val contentImageUrl: String? = null,
    val isContentSummary: Boolean,
    val department: String,
    val registrationDate: LocalDate,
    val isAttachment: Boolean,
    val topic: CrawlableType,
    val topicId: Int

) {

    companion object {

        fun from(notice: Notice): NoticeResponseV2 =
            NoticeResponseV2(
                nttId = notice.nttId,
                title = notice.title,
                contentUrl = notice.contentUrl,
                contentImageUrl = notice.contentImageUrl,
                isContentSummary = notice.contentSummary != null,
                department = notice.department,
                registrationDate = notice.registrationDate,
                isAttachment = notice.isAttachment,
                topic = notice.topic,
                topicId = notice.topic.code
            )

        fun from(notices: List<Notice>): List<NoticeResponseV2> =
            notices.map { this.from(it) }
    }

}
