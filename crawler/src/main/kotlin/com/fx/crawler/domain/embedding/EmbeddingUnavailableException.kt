package com.fx.crawler.domain.embedding

/**
 * 지금은 임베딩을 진행할 수 없다. 호출 한도(429)에 걸렸거나, Gemini · 학교 사이트 장애로 요청을 잠시 멈춘 동안이다.
 * 공지의 문제가 아니므로 시도 횟수로 세지 않고 다음 실행으로 미룬다.
 */
class EmbeddingUnavailableException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
