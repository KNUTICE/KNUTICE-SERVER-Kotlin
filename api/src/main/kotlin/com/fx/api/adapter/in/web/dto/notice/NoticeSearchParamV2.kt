package com.fx.api.adapter.`in`.web.dto.notice

import com.fx.api.domain.NoticeQuery
import com.fx.global.domain.CrawlableType
import org.springframework.data.domain.Pageable

data class NoticeSearchParamV2(

    val nttId: Long? = null,
    val topic: Int? = null,
    val keyword: String? = null

) {
    fun toCommand(pageable: Pageable) =
        NoticeQuery(
            nttId = this.nttId,
            topic = topic?.let { CrawlableType.fromCode(it) },
            keyword = this.keyword,
            pageable = pageable
        )
}
