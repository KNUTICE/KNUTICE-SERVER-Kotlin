package com.fx.crawler.application.port.out

import com.fx.crawler.domain.embedding.EmbeddingInput
import com.fx.crawler.domain.embedding.EmbeddingRejectedException
import com.fx.crawler.domain.embedding.EmbeddingUnavailableException

/** 공지 청크 임베딩 (텍스트 · 이미지). */
interface NoticeEmbeddingPort {

    /** `notice_chunk.embedding_model` 에 저장하는 모델 · 차원 표기 (예: `gemini-embedding-2@768`). */
    val modelTag: String

    /**
     * 지금 임베딩을 요청할 수 있는지 확인한다. 이미지를 받는 등 임베딩 전에 하는 일을 멈춘 동안 하지 않으려고 먼저 부른다.
     * @throws EmbeddingUnavailableException 호출 한도 · 장애로 요청을 멈춘 동안
     */
    fun checkAvailable()

    /**
     * 입력마다 길이 1 로 정규화한 벡터를 입력 순서대로 돌려준다. 일시적인 오류는 몇 번 다시 시도한다.
     * @throws EmbeddingUnavailableException 호출 한도에 걸렸거나 요청을 멈춘 동안
     * @throws EmbeddingRejectedException 모델이 입력을 받지 않았을 때 (400)
     * @throws RuntimeException 끝내 임베딩하지 못했을 때
     */
    fun embed(inputs: List<EmbeddingInput>): List<FloatArray>

}
