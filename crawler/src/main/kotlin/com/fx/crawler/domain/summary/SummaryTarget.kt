package com.fx.crawler.domain.summary

/** AI 요약 대상 공지. */
data class SummaryTarget(
    val noticeId: Long,
    val nttId: Long,
    val topicCode: Int,
    val title: String,
    val content: String?,
)
