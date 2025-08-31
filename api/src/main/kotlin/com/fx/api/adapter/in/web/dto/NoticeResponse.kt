package com.fx.api.adapter.`in`.web.dto

import com.fx.global.domain.CrawlableType
import com.fx.global.domain.Notice
import java.time.LocalDate

data class NoticeResponse(

    val nttId: Long,
    val title: String,
    val contentUrl: String,
    val contentImageUrl: String? = null,
    val department: String,
    val registrationDate: LocalDate,
    val noticeType: CrawlableType

) {

    companion object {

        fun from(notice: Notice): NoticeResponse =
            NoticeResponse(
                nttId = notice.nttId,
                title = notice.title,
                contentUrl = notice.contentUrl,
                contentImageUrl = notice.contentImageUrl,
                department = notice.department,
                registrationDate = notice.registrationDate,
                noticeType = notice.type
            )

        fun from(notices: List<Notice>): List<NoticeResponse> =
            notices.map { this.from(it) }
    }

}
