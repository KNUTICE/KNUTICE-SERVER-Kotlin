package com.fx.global.domain

import org.springframework.data.domain.Pageable

data class NoticeQuery(

    val nttId: Long? = null,
    val topic: CrawlableType? = null,
    val keyword: String? = null,
    val pageable: Pageable

)