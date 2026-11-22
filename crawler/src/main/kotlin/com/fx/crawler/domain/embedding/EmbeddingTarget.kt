package com.fx.crawler.domain.embedding

/** 임베딩 대상 공지. 본문은 크롤러가 저장한 텍스트이고, 이미지는 [contentUrl] 상세 페이지에서 다시 읽는다. */
data class EmbeddingTarget(
    val noticeId: Long,
    val nttId: Long,
    val topicCode: Int,
    val topicName: String,
    val title: String,
    val contentUrl: String,
    val content: String?,
)
