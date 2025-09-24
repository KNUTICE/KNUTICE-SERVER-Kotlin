package com.fx.global.domain

import java.time.LocalDate
import java.time.LocalDateTime

data class Notice(
    val nttId: Long,
    val title: String,
    val department: String,
    val contentUrl: String,
    val content: String? = null,
    val contentSummary: String? = null,
    val contentImageUrl: String? = null,
    val registrationDate: LocalDate,
    val isAttachment: Boolean,
    val topic: CrawlableType, // 일반공지, 학사공지 등....

    val createdAt: LocalDateTime? = null, // nttId(@Id) 를 미리 지정한 경우 createdAt 생성 불가.
    val updatedAt: LocalDateTime? = null
) {

    fun withDetail(content: String?, contentImageUrl: String?): Notice =
        this.copy(
            content = content,
            contentImageUrl = contentImageUrl
        )

}