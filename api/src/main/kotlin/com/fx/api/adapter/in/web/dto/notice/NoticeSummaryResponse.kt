package com.fx.api.adapter.`in`.web.dto.notice

import com.fx.global.domain.Notice

data class NoticeSummaryResponse(

    val nttId: Long,
    val contentSummary: String? = null

) {
    companion object {
        fun from(notice: Notice): NoticeSummaryResponse =
            NoticeSummaryResponse(
                nttId = notice.nttId,
                contentSummary = notice.contentSummary
            )
    }

}
