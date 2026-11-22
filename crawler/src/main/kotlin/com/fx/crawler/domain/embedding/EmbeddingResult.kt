package com.fx.crawler.domain.embedding

/** 임베딩한 청크 하나. [vector] 는 길이 1 로 정규화돼 있다. */
class EmbeddedChunk(
    val draft: ChunkDraft,
    val vector: FloatArray,
)

/** 공지 하나의 임베딩 결과. */
sealed interface EmbeddingResult {

    val noticeId: Long

    /** 청크를 모두 임베딩했다. 저장할 때 그 공지의 옛 청크를 통째로 바꾼다. */
    class Completed(override val noticeId: Long, val chunks: List<EmbeddedChunk>) : EmbeddingResult

    /** 임베딩 · 학교 사이트 호출이 실패했다. 시도 횟수가 한도에 닿기 전까지 다음 실행에서 다시 시도한다. */
    data class Failed(override val noticeId: Long, val reason: String) : EmbeddingResult

    /** 호출 한도에 걸려 이번에는 임베딩하지 않았다. 상태 · 시도 횟수를 바꾸지 않고 다음 실행에서 다시 한다. */
    data class Deferred(override val noticeId: Long) : EmbeddingResult

}
