package com.fx.crawler.domain.embedding

/**
 * 임베딩 모델이 입력을 받지 않았다 (400). 다시 보내도 같으므로 재시도하지 않는다.
 * 이미지면 그 이미지만 빼고, 본문이면 그 공지를 실패로 센다.
 */
class EmbeddingRejectedException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
