package com.fx.crawler.domain.summary

/** AI 요약 호출 한도(분당 · 일일 요청 수 등)에 걸렸다. 공지의 문제가 아니므로 시도 횟수로 세지 않는다. */
class SummaryRateLimitedException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
