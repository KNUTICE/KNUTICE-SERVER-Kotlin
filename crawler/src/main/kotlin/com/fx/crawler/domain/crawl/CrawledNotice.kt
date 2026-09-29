package com.fx.crawler.domain.crawl

import com.fx.common.domain.catalog.TopicView
import java.time.LocalDate

/**
 * 게시판 목록에서 읽은 공지. 본문 · 첫 이미지는 상세 페이지를 읽은 뒤 [withDetail] 로 채운다.
 */
data class CrawledNotice(
    val nttId: Long,
    val topic: TopicView,
    val title: String,
    val department: String,
    val contentUrl: String,
    val registrationDate: LocalDate,
    val isAttachment: Boolean,
    val content: String? = null,
    val contentImageUrl: String? = null,
) {

    fun withDetail(detail: NoticeDetail): CrawledNotice =
        copy(content = detail.content, contentImageUrl = detail.contentImageUrl)

}
