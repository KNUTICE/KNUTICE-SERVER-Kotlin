package com.fx.common.domain.notice

/** 공지 RAG 임베딩 상태. 임베딩 Job 이 [PENDING] 공지를 찾아 청크를 만든다. */
enum class EmbeddingStatus {

    /** 임베딩 대기. 다음 임베딩 배치에서 시도한다. 모델을 바꾸면 이 상태로 되돌려 다시 임베딩한다. */
    PENDING,

    /** 청크 저장 완료. */
    COMPLETED,

    /** 시도 한도까지 실패. 더 시도하지 않는다. */
    FAILED,

}
