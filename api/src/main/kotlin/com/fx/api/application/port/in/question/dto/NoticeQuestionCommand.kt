package com.fx.api.application.port.`in`.question.dto

import com.fx.api.domain.question.NoticeAnswerProvider

/** 질문한 사람. 관리자는 user.id, 앱은 fcmToken 으로 구분한다 (fcmToken 원문은 저장하지 않는다) */
sealed interface QuestionAsker {

    data class Admin(val userId: String) : QuestionAsker

    data class App(val fcmToken: String) : QuestionAsker {
        /** 로그 · 예외 메시지에 기기 토큰이 찍히지 않게 한다 */
        override fun toString(): String =
            "App(fcmToken=***)"
    }

}

/**
 * 질문 준비 (검색 · 프롬프트). 검색 설정은 관리자 시험 화면만 바꿀 수 있고, 비우면 서버 기본값을 쓴다.
 */
data class PrepareQuestionCommand(
    val asker: QuestionAsker,
    val question: String,
    val decayPerYear: Double? = null,
    val titlePenalty: Double? = null,
    val topNotices: Int? = null,
)

/**
 * 사용자 키로 받은 답변 저장.
 *
 * @property finishToken prepare 응답의 서명. 질문 · 질문한 사람 · 만료 시각을 묶는다
 * @property inputTokens · outputTokens 앱 · 웹이 AI 응답에서 읽은 토큰 수 (모르면 null)
 */
data class FinishQuestionCommand(
    val asker: QuestionAsker,
    val questionId: Long,
    val answer: String,
    val provider: NoticeAnswerProvider,
    val model: String,
    val finishToken: String,
    val inputTokens: Int? = null,
    val outputTokens: Int? = null,
)
