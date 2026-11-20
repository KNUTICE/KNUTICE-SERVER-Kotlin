package com.fx.api.application.port.`in`.question

import com.fx.api.application.port.`in`.question.dto.FinishQuestionCommand
import com.fx.api.application.port.`in`.question.dto.PrepareQuestionCommand
import com.fx.api.application.port.`in`.question.dto.QuestionAsker
import com.fx.api.domain.question.FinishedQuestion
import com.fx.api.domain.question.NoticeQuestionVerdict
import com.fx.api.domain.question.PreparedQuestion
import com.fx.api.domain.question.QuestionQuota
import com.fx.api.exception.QuestionException
import com.fx.common.exception.FcmTokenException

/**
 * 공지 질문(RAG). 답변은 서버가 만들지 않고, 앱 · 관리자 웹이 사용자 개인 AI 키로 받는다.
 *
 * 1. [prepare]: 한도 확인 → 질문 임베딩(서버 키) → 검색 → 근거 공지 · 프롬프트 · 서명
 * 2. 앱 · 웹이 프롬프트로 팩트챗 · Gemini 를 직접 부른다 (키는 기기 · 브라우저에만)
 * 3. [finish]: 서명 확인 → 없는 근거 번호 정리 → 답변 저장
 */
interface NoticeQuestionUseCase {

    /**
     * @throws QuestionException 하루 한도 (DAILY_LIMIT_EXCEEDED · SERVICE_DAILY_LIMIT_EXCEEDED), 임베딩 한도 · 장애 (AI_RATE_LIMITED · AI_UNAVAILABLE)
     * @throws FcmTokenException 앱의 기기 토큰이 등록되지 않았을 때 (TOKEN_NOT_FOUND)
     */
    fun prepare(command: PrepareQuestionCommand): PreparedQuestion

    /**
     * @throws QuestionException 다른 사람 · 없는 질문 (QUESTION_NOT_FOUND), 서명 오류 · 만료 (FINISH_TOKEN_INVALID),
     *   이미 저장 · 근거 없음 (ALREADY_FINISHED), 빈 답변 (ANSWER_EMPTY)
     */
    fun finish(command: FinishQuestionCommand): FinishedQuestion

    /** 오늘(한국시간 초기화 시각 기준) 질문 사용량 */
    fun getQuota(asker: QuestionAsker): QuestionQuota

    /** @throws QuestionException 다른 사람 · 없는 질문 (QUESTION_NOT_FOUND) */
    fun giveFeedback(asker: QuestionAsker, questionId: Long, verdict: NoticeQuestionVerdict, memo: String?): Boolean

}
