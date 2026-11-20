package com.fx.api.application.port.`in`.question

import com.fx.api.application.port.`in`.question.dto.NoticeQuestionCommand
import com.fx.api.domain.question.NoticeAnswer
import com.fx.api.domain.question.NoticeQuestionVerdict
import com.fx.api.domain.question.QuestionQuota
import com.fx.api.exception.QuestionException

interface NoticeQuestionUseCase {

    /**
     * 질문을 임베딩해 관련 공지를 찾고, 근거가 있으면 답변을 만든다. 처리한 질문은 기록으로 남긴다.
     *
     * @throws QuestionException 하루 한도를 넘었을 때 (DAILY_LIMIT_EXCEEDED), Gemini 한도 · 장애일 때 (AI_RATE_LIMITED · AI_UNAVAILABLE)
     */
    fun ask(command: NoticeQuestionCommand): NoticeAnswer

    /** 오늘(한국시간 초기화 시각 기준) 질문 사용량 */
    fun getQuota(): QuestionQuota

    /**
     * 질문 기록에 관리자 판정과 메모를 남긴다.
     *
     * @throws QuestionException 기록이 없을 때 (QUESTION_NOT_FOUND)
     */
    fun giveFeedback(questionId: Long, verdict: NoticeQuestionVerdict, memo: String?): Boolean

}
