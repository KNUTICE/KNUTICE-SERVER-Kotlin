package com.fx.crawler.application.port.`in`

import com.fx.crawler.domain.embedding.EmbeddingResult
import com.fx.crawler.domain.embedding.EmbeddingTarget

interface NoticeEmbeddingUseCase {

    /** 임베딩 대기 공지를 최신 게시글부터. 실패한 적이 있는 공지는 재시도 간격이 지난 것만 포함한다. */
    fun findTargets(beforeNttId: Long?, size: Int): List<EmbeddingTarget>

    /** 공지 하나를 청크로 나눠 임베딩한다. 실패해도 예외를 던지지 않고 결과로 돌려준다. */
    fun embed(target: EmbeddingTarget): EmbeddingResult

    /** 청크를 바꿔 넣고 공지의 임베딩 상태를 바꾼다. */
    fun applyResults(results: List<EmbeddingResult>)

}
