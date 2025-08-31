package com.fx.api.adapter.`in`.web.dto

import com.fx.api.domain.NoticeQuery
import com.fx.global.domain.CrawlableType
import org.springframework.data.domain.Pageable

data class NoticeSearchParam(

    val nttId: Long? = null,
    val type: CrawlableType? = null,
    val keyword: String? = null

) {
    fun toCommand(pageable: Pageable) =
        NoticeQuery(
            nttId = this.nttId,
            type = type,
            keyword = this.keyword,
            pageable = pageable
        )
}