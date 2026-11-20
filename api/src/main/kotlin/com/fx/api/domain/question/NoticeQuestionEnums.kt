package com.fx.api.domain.question

/** 질문한 쪽 */
enum class NoticeQuestionAsker {

    /** 관리자 시험 화면 (asker_key = user.id) */
    ADMIN,

    /** 앱 (asker_key = fcmToken 의 HMAC) */
    APP,

}

/** 질문 처리 상태 */
enum class NoticeQuestionStatus {

    /** 근거 공지와 프롬프트를 돌려줬고, 앱 · 웹이 답변을 받아 오길 기다린다 */
    PREPARED,

    /** 사용자 키로 받은 답변을 저장했다 */
    ANSWERED,

    /** 근거로 쓸 만큼 비슷한 공지가 없다 (AI 를 부를 필요 없음) */
    NOT_FOUND,

}

/** 답변을 만든 AI 공급자. 키는 사용자 기기 · 브라우저에만 있고 서버는 받지 않는다 */
enum class NoticeAnswerProvider {

    /** 학교 팩트챗(FactChat) Gateway — 기본 */
    FACTCHAT,

    /** Google AI Studio 무료 키 */
    GEMINI,

}

/** 답변 판정 */
enum class NoticeQuestionVerdict {
    GOOD,
    BAD,
}
