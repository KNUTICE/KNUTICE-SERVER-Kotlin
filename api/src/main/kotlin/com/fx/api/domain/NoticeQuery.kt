package com.fx.api.domain

import com.fx.global.domain.CrawlableType
import org.springframework.data.domain.Pageable

data class NoticeQuery(

    val nttId: Long? = null,
    val type: CrawlableType? = null,
    val keyword: String? = null,
    val pageable: Pageable

)