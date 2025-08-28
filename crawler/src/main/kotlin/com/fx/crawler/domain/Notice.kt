package com.fx.crawler.domain

import java.time.LocalDate
import java.time.LocalDateTime

data class Notice(
    val nttId: Long,
    val title: String,
    val department: String,
    val contentUrl: String,
    val contentImage: String? = null,
    val registrationDate: LocalDate,
    val isAttachment: Boolean,
    val type: CrawlableType, // 일반공지, 학사공지 등....

    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)