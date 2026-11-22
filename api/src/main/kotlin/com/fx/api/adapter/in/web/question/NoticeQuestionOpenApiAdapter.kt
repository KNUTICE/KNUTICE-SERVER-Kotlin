package com.fx.api.adapter.`in`.web.question

import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionFeedbackRequest
import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionFinishRequest
import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionFinishResponse
import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionPrepareRequest
import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionPrepareResponse
import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionQuotaResponse
import com.fx.api.application.port.`in`.question.NoticeQuestionUseCase
import com.fx.api.application.port.`in`.question.dto.QuestionAsker
import com.fx.api.config.question.NoticeQuestionProperties
import com.fx.common.annotation.hexagonal.WebInputAdapter
import io.github.seob7.Api
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping

/**
 * 앱(학생)용 공지 질문. 앱은 열람실처럼 `fcmToken` 헤더로 기기를 알린다.
 * 답변은 앱이 학생 본인의 AI 키(팩트챗 · Gemini)로 직접 받는다. 키는 휴대폰에만 있고 서버로 보내지 않는다.
 */
@WebInputAdapter
@RequestMapping("/open-api/v1/notice-questions")
class NoticeQuestionOpenApiAdapter(
    private val noticeQuestionUseCase: NoticeQuestionUseCase,
    private val properties: NoticeQuestionProperties,
) : NoticeQuestionOpenApiSwagger {

    @PostMapping("/prepare")
    override fun prepare(
        @RequestHeader fcmToken: String,
        @RequestBody @Valid request: NoticeQuestionPrepareRequest,
    ): ResponseEntity<Api<NoticeQuestionPrepareResponse>> =
        Api.OK(
            NoticeQuestionPrepareResponse.from(
                noticeQuestionUseCase.prepare(request.toCommand(fcmToken)),
                properties.answerTemperature,
                properties.answerMaxOutputTokens,
                withDebug = false,
            ),
            "질문을 준비했습니다."
        )

    @PostMapping("/{questionId}/finish")
    override fun finish(
        @RequestHeader fcmToken: String,
        @PathVariable questionId: String,
        @RequestBody @Valid request: NoticeQuestionFinishRequest,
    ): ResponseEntity<Api<NoticeQuestionFinishResponse>> =
        Api.OK(
            NoticeQuestionFinishResponse.from(
                noticeQuestionUseCase.finish(request.toCommand(QuestionAsker.App(fcmToken), parseQuestionId(questionId)))
            ),
            "답변을 저장했습니다."
        )

    @GetMapping("/usage")
    override fun getUsage(@RequestHeader fcmToken: String): ResponseEntity<Api<NoticeQuestionQuotaResponse>> =
        Api.OK(NoticeQuestionQuotaResponse.from(noticeQuestionUseCase.getQuota(QuestionAsker.App(fcmToken))), "질문 사용량 조회 성공")

    @PatchMapping("/{questionId}/feedback")
    override fun giveFeedback(
        @RequestHeader fcmToken: String,
        @PathVariable questionId: String,
        @RequestBody @Valid request: NoticeQuestionFeedbackRequest,
    ): ResponseEntity<Api<Boolean>> =
        Api.OK(
            noticeQuestionUseCase.giveFeedback(
                QuestionAsker.App(fcmToken), parseQuestionId(questionId), requireNotNull(request.verdict), request.memo,
            ),
            "판정을 남겼습니다."
        )

}
