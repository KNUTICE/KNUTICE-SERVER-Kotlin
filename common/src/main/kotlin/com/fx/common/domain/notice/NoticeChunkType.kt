package com.fx.common.domain.notice

/** RAG 임베딩 청크 종류 (`notice_chunk.chunk_type`). */
enum class NoticeChunkType {

    /** 본문 · 이미지가 모두 없는 공지. 제목만으로 한 청크를 만든다. */
    TITLE,

    /** 본문을 나눈 조각. 검색 문맥을 위해 조각 앞에 제목을 붙인다. */
    BODY,

    /** 본문 이미지. 벡터는 이미지로 만들고, 내용에는 답변 때 함께 보여 줄 제목을 둔다. */
    IMAGE,

}
