package com.fx.api.adapter.`in`.web.question

import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionFeedbackRequest
import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionFinishRequest
import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionFinishResponse
import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionPrepareRequest
import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionPrepareResponse
import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionQuotaResponse
import com.fx.api.exception.errorcode.QuestionErrorCode
import com.fx.common.annotation.ApiExceptionExplanation
import com.fx.common.annotation.ApiResponseExplanations
import com.fx.common.exception.errorcode.FcmTokenErrorCode
import io.github.seob7.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@Tag(name = "공지 질문(RAG) API - 앱")
interface NoticeQuestionOpenApiSwagger {

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(name = "등록되지 않은 기기", description = "fcmToken 이 등록되지 않은 경우", value = FcmTokenErrorCode::class, constant = "TOKEN_NOT_FOUND"),
            ApiExceptionExplanation(name = "기기 하루 한도", description = "이 기기의 오늘 질문 수가 상한에 닿은 경우 (근거를 못 찾은 질문은 세지 않음)", value = QuestionErrorCode::class, constant = "DAILY_LIMIT_EXCEEDED"),
            ApiExceptionExplanation(name = "서버 하루 한도", description = "서버 전체 하루 질문 수가 상한에 닿은 경우", value = QuestionErrorCode::class, constant = "SERVICE_DAILY_LIMIT_EXCEEDED"),
            ApiExceptionExplanation(name = "임베딩 한도", description = "질문 분석(서버 무료 키)이 호출 한도에 걸린 경우. 횟수에 세지 않는다", value = QuestionErrorCode::class, constant = "AI_RATE_LIMITED"),
            ApiExceptionExplanation(name = "임베딩 장애", description = "질문 분석 서버에 연결하지 못한 경우. 횟수에 세지 않는다", value = QuestionErrorCode::class, constant = "AI_UNAVAILABLE"),
        ]
    )
    @Operation(
        summary = "공지 질문 준비",
        description = "질문으로 근거 공지를 찾고, AI 에 넘길 프롬프트(prompt.messages)와 finishToken 을 돌려줍니다. " +
            "앱은 학생 본인의 키로 팩트챗(기본, OpenAI 형식 POST /chat/completions/) 또는 Gemini(generateContent)를 직접 부르고, " +
            "받은 답을 10분 안에 finish 로 보냅니다. 키는 휴대폰에만 두고 서버로 보내지 않습니다. " +
            "status=NOT_FOUND 면 prompt 가 없고 AI 를 부를 필요가 없습니다 (sources 는 비슷한 공지). 기기마다 하루 3회, 한국시간 0시 초기화."
    )
    fun prepare(
        @RequestHeader fcmToken: String,
        @RequestBody @Valid request: NoticeQuestionPrepareRequest,
    ): ResponseEntity<Api<NoticeQuestionPrepareResponse>>

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(name = "질문 없음", description = "없는 질문이거나 다른 기기의 질문인 경우", value = QuestionErrorCode::class, constant = "QUESTION_NOT_FOUND"),
            ApiExceptionExplanation(name = "서명 오류", description = "finishToken 이 맞지 않거나 만료된 경우. 다시 질문해야 한다", value = QuestionErrorCode::class, constant = "FINISH_TOKEN_INVALID"),
            ApiExceptionExplanation(name = "이미 저장", description = "이미 답변을 저장했거나 근거가 없던 질문인 경우", value = QuestionErrorCode::class, constant = "ALREADY_FINISHED"),
            ApiExceptionExplanation(name = "빈 답변", description = "근거 번호를 지우고 나니 답변이 비어 있는 경우", value = QuestionErrorCode::class, constant = "ANSWER_EMPTY"),
        ]
    )
    @Operation(
        summary = "답변 저장",
        description = "앱이 받은 답변을 저장합니다. 근거 목록에 없는 [n] 번호는 지우고, 화면에 보여 줄 정리된 답변과 답변이 쓴 근거 공지를 돌려줍니다."
    )
    fun finish(
        @RequestHeader fcmToken: String,
        @PathVariable questionId: String,
        @RequestBody @Valid request: NoticeQuestionFinishRequest,
    ): ResponseEntity<Api<NoticeQuestionFinishResponse>>

    @Operation(summary = "오늘 질문 사용량 (이 기기)", description = "한국시간 0시에 초기화됩니다.")
    fun getUsage(@RequestHeader fcmToken: String): ResponseEntity<Api<NoticeQuestionQuotaResponse>>

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(name = "질문 없음", description = "없는 질문이거나 다른 기기의 질문인 경우", value = QuestionErrorCode::class, constant = "QUESTION_NOT_FOUND"),
        ]
    )
    @Operation(summary = "답변 판정 남기기", description = "맞음(GOOD) · 틀림(BAD)과 메모를 남깁니다. 다시 남기면 덮어씁니다.")
    fun giveFeedback(
        @RequestHeader fcmToken: String,
        @PathVariable questionId: String,
        @RequestBody @Valid request: NoticeQuestionFeedbackRequest,
    ): ResponseEntity<Api<Boolean>>

}
