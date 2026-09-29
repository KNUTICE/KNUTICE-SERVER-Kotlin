package com.fx.crawler.domain.crawl

/** 공지 상세 페이지에서 읽은 본문 텍스트와 첫 이미지. */
data class NoticeDetail(
    val content: String?,
    val contentImageUrl: String?,
) {

    companion object {

        /** 상세 페이지를 읽지 못했을 때. 공지는 본문 없이 저장하고 요약은 건너뛴다. */
        val EMPTY = NoticeDetail(content = null, contentImageUrl = null)

    }

}
