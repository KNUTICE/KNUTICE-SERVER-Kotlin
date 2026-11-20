package com.fx.api.domain.question

/** 질문한 쪽. 지금은 관리자 시험 화면만 있다. */
enum class NoticeQuestionAsker {
    ADMIN,
}

/** 질문 처리 결과 */
enum class NoticeQuestionStatus {

    /** 근거 공지로 답변을 만들었다 */
    ANSWERED,

    /** 근거로 쓸 만큼 비슷한 공지가 없어 답변을 만들지 않았다 */
    NOT_FOUND,

    /** 검색만 했다 (답변 만들기를 끈 요청) */
    SEARCH_ONLY,

}

/** 관리자가 남긴 답변 판정 */
enum class NoticeQuestionVerdict {
    GOOD,
    BAD,
}
