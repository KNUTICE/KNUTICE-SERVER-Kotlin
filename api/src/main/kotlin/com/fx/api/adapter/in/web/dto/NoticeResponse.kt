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

        fun from(notices: List<Notice>): List<NoticeResponse> =
            notices.map {
                NoticeResponse(
                    nttId = it.nttId,
                    title = it.title,
                    contentUrl = it.contentUrl,
                    contentImageUrl = it.contentImageUrl,
                    department = it.department,
                    registrationDate = it.registrationDate,
                    noticeType = it.type
                )
            }
    }

}
