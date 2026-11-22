package com.fx.api.adapter.`in`.web.question

import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionAdminPrepareRequest
import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionFeedbackRequest
import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionFinishRequest
import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionFinishResponse
import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionPrepareResponse
import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionQuotaResponse
import com.fx.api.adapter.`in`.web.question.dto.NoticeSearchCompareRequest
import com.fx.api.adapter.`in`.web.question.dto.NoticeSearchCompareResponse
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
            ApiExceptionExplanation(name = "검색 비교 하루 한도", description = "오늘 새로 임베딩한 질문 수가 상한에 닿은 경우. 이미 검색한 질문은 계속 비교할 수 있다", value = QuestionErrorCode::class, constant = "COMPARE_DAILY_LIMIT_EXCEEDED"),
            ApiExceptionExplanation(name = "임베딩 한도", description = "질문 임베딩(서버 무료 키)이 호출 한도(429)에 걸린 경우. 횟수에 남지 않는다", value = QuestionErrorCode::class, constant = "AI_RATE_LIMITED"),
            ApiExceptionExplanation(name = "임베딩 장애", description = "서버 키가 없거나 Gemini 가 응답하지 못한 경우. 횟수에 남지 않는다", value = QuestionErrorCode::class, constant = "AI_UNAVAILABLE"),
        ]
    )
    @Operation(
        summary = "검색 비교 (관리자, 답변 없음)",
        description = "질문을 한 번 임베딩해 A(유사도만)와 B(검색 설정 적용, 비우면 서버 기본값)로 두 번 검색하고 공지 단위로 나란히 돌려줍니다. " +
            "질문 기록을 남기지 않고 질문 한도에도 세지 않습니다. 같은 질문은 서버 메모리에 둔 임베딩을 다시 써서 검색 설정만 바꿔 비교할 때는 서버 키를 쓰지 않습니다."
    )
    fun compareSearch(
        @RequestBody @Valid request: NoticeSearchCompareRequest,
    ): ResponseEntity<Api<NoticeSearchCompareResponse>>

    @Operation(summary = "오늘 검색 비교 사용량", description = "새로 임베딩한 질문 수입니다. 서버 메모리에서 세므로 재시작하면 0 이 됩니다.")
    fun getSearchCompareUsage(): ResponseEntity<Api<NoticeQuestionQuotaResponse>>

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(name = "관리자 하루 한도", description = "관리자 전체 하루 질문 수가 상한에 닿은 경우", value = QuestionErrorCode::class, constant = "DAILY_LIMIT_EXCEEDED"),
            ApiExceptionExplanation(name = "서버 하루 한도", description = "서버 전체 하루 질문 수(임베딩 무료 한도 보호)가 상한에 닿은 경우", value = QuestionErrorCode::class, constant = "SERVICE_DAILY_LIMIT_EXCEEDED"),
            ApiExceptionExplanation(name = "임베딩 한도", description = "질문 임베딩(서버 무료 키)이 호출 한도(429)에 걸린 경우. 기록 · 횟수에 남지 않는다", value = QuestionErrorCode::class, constant = "AI_RATE_LIMITED"),
            ApiExceptionExplanation(name = "임베딩 장애", description = "서버 키가 없거나 Gemini 가 응답하지 못한 경우. 기록 · 횟수에 남지 않는다", value = QuestionErrorCode::class, constant = "AI_UNAVAILABLE"),
        ]
    )
    @Operation(
        summary = "공지 질문 준비 (관리자)",
        description = "질문을 임베딩해 근거 공지를 찾고, AI 에 넘길 프롬프트(prompt.messages)와 finishToken 을 돌려줍니다. " +
            "답변은 서버가 만들지 않습니다. 관리자 웹이 개인 AI 키(팩트챗 · Gemini)로 직접 받아 finish 로 보냅니다. " +
            "가장 비슷한 공지도 기준보다 낮으면 status=NOT_FOUND (prompt 없음). 검색 설정을 비우면 서버 기본값을 씁니다."
    )
    fun prepare(
        @Parameter(hidden = true) user: AuthenticatedUser,
        @RequestBody @Valid request: NoticeQuestionAdminPrepareRequest,
    ): ResponseEntity<Api<NoticeQuestionPrepareResponse>>

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(name = "질문 없음", description = "없는 질문이거나 다른 사람의 질문인 경우", value = QuestionErrorCode::class, constant = "QUESTION_NOT_FOUND"),
            ApiExceptionExplanation(name = "서명 오류", description = "finishToken 이 맞지 않거나 만료된 경우", value = QuestionErrorCode::class, constant = "FINISH_TOKEN_INVALID"),
            ApiExceptionExplanation(name = "이미 저장", description = "이미 답변을 저장했거나 근거가 없던(NOT_FOUND) 질문인 경우", value = QuestionErrorCode::class, constant = "ALREADY_FINISHED"),
            ApiExceptionExplanation(name = "빈 답변", description = "근거 번호를 지우고 나니 답변이 비어 있는 경우", value = QuestionErrorCode::class, constant = "ANSWER_EMPTY"),
        ]
    )
    @Operation(
        summary = "답변 저장 (관리자)",
        description = "개인 키로 받은 답변을 저장합니다. 키는 보내지 않습니다. 근거 목록에 없는 [n] 번호는 지우고, 정리한 답변과 쓴 근거 공지를 돌려줍니다."
    )
    fun finish(
        @Parameter(hidden = true) user: AuthenticatedUser,
        @PathVariable questionId: String,
        @RequestBody @Valid request: NoticeQuestionFinishRequest,
    ): ResponseEntity<Api<NoticeQuestionFinishResponse>>

    @Operation(summary = "오늘 질문 사용량 (관리자 전체)", description = "한국시간 0시에 초기화됩니다.")
    fun getUsage(@Parameter(hidden = true) user: AuthenticatedUser): ResponseEntity<Api<NoticeQuestionQuotaResponse>>

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(name = "질문 없음", description = "없는 질문이거나 다른 사람의 질문인 경우", value = QuestionErrorCode::class, constant = "QUESTION_NOT_FOUND"),
        ]
    )
    @Operation(summary = "답변 판정 남기기 (관리자)", description = "맞음(GOOD) · 틀림(BAD)과 메모를 남깁니다. 다시 남기면 덮어씁니다.")
    fun giveFeedback(
        @Parameter(hidden = true) user: AuthenticatedUser,
        @PathVariable questionId: String,
        @RequestBody @Valid request: NoticeQuestionFeedbackRequest,
    ): ResponseEntity<Api<Boolean>>

}
