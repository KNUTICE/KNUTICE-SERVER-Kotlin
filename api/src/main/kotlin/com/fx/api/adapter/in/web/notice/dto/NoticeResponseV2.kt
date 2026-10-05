package com.fx.api.adapter.`in`.web.notice.dto

import com.fx.common.domain.notice.Notice
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
    /** v1 토픽 이름 (예: `GENERAL_NEWS`) */
    val topic: String,
    /** v2 토픽 코드 */
    val topicId: Int,
) {

    companion object {

        fun from(notice: Notice): NoticeResponseV2 =
            NoticeResponseV2(
                nttId = notice.nttId,
                title = notice.title,
                contentUrl = notice.contentUrl,
                contentImageUrl = notice.contentImageUrl,
                isContentSummary = notice.hasSummary,
                department = notice.department,
                registrationDate = notice.registrationDate,
                isAttachment = notice.isAttachment,
                topic = notice.topicName,
                topicId = notice.topicCode,
            )

        fun from(notices: List<Notice>): List<NoticeResponseV2> =
            notices.map {
                this.from(it)
            }
    }

}
