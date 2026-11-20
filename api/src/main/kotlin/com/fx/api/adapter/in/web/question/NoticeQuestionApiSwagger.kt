package com.fx.api.adapter.`in`.web.question

import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionFeedbackRequest
import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionQuotaResponse
import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionRequest
import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionResponse
import com.fx.api.config.security.dto.AuthenticatedUser
import com.fx.api.exception.errorcode.QuestionErrorCode
import com.fx.common.annotation.ApiExceptionExplanation
import com.fx.common.annotation.ApiResponseExplanations
import io.github.seob7.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "공지 질문(RAG) API - ADMIN")
interface NoticeQuestionApiSwagger {

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "하루 한도 초과",
                description = "오늘(한국시간 17시 기준) 질문 수가 상한에 닿은 경우",
                value = QuestionErrorCode::class,
                constant = "DAILY_LIMIT_EXCEEDED"
            ),
            ApiExceptionExplanation(
                name = "AI 호출 한도",
                description = "Gemini 가 호출 한도(429)를 알려 온 경우. 횟수에는 세지 않는다",
                value = QuestionErrorCode::class,
                constant = "AI_RATE_LIMITED"
            ),
            ApiExceptionExplanation(
                name = "AI 장애",
                description = "키가 없거나 Gemini 가 요청을 거절 · 응답하지 못한 경우. 횟수에는 세지 않는다",
                value = QuestionErrorCode::class,
                constant = "AI_UNAVAILABLE"
            ),
        ]
    )
    @Operation(
        summary = "공지 질문",
        description = "질문을 임베딩해 관련 공지를 찾고, 근거가 있으면 답변을 만듭니다. " +
            "근거 공지 중 가장 높은 유사도가 기준보다 낮으면 답변 없이 status=NOT_FOUND 로 응답합니다. " +
            "검색 설정(decayPerYear · titlePenalty · topNotices)을 비우면 서버 기본값을 씁니다."
    )
    fun ask(
        @Parameter(hidden = true) user: AuthenticatedUser,
        @RequestBody @Valid request: NoticeQuestionRequest,
    ): ResponseEntity<Api<NoticeQuestionResponse>>

    @Operation(summary = "오늘 질문 사용량", description = "한국시간 17시에 초기화됩니다 (Gemini 무료 한도 초기화 시각).")
    fun getUsage(): ResponseEntity<Api<NoticeQuestionQuotaResponse>>

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "기록 없음",
                description = "질문 기록이 없는 경우",
                value = QuestionErrorCode::class,
                constant = "QUESTION_NOT_FOUND"
            ),
        ]
    )
    @Operation(summary = "답변 판정 남기기", description = "맞음(GOOD) · 틀림(BAD)과 메모를 남깁니다. 다시 남기면 덮어씁니다.")
    fun giveFeedback(
        @PathVariable questionId: String,
        @RequestBody @Valid request: NoticeQuestionFeedbackRequest,
    ): ResponseEntity<Api<Boolean>>

}
