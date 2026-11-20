package com.fx.api.adapter.`in`.web.question

import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionAdminPrepareRequest
import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionFeedbackRequest
import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionFinishRequest
import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionFinishResponse
import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionPrepareResponse
import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionQuotaResponse
import com.fx.api.application.port.`in`.question.NoticeQuestionUseCase
import com.fx.api.application.port.`in`.question.dto.QuestionAsker
import com.fx.api.config.question.NoticeQuestionProperties
import com.fx.api.config.security.dto.AuthenticatedUser
import com.fx.common.annotation.hexagonal.WebInputAdapter
import io.github.seob7.Api
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

/** 관리자 시험 화면용. 앱용([NoticeQuestionOpenApiAdapter])과 같은 흐름에 검색 설정 · 검색 정보(debug)가 더 있다 */
@WebInputAdapter
@RequestMapping("/api/v1/notice-questions")
class NoticeQuestionApiAdapter(
    private val noticeQuestionUseCase: NoticeQuestionUseCase,
    private val properties: NoticeQuestionProperties,
) : NoticeQuestionApiSwagger {

    @PostMapping("/prepare")
    override fun prepare(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @RequestBody @Valid request: NoticeQuestionAdminPrepareRequest,
    ): ResponseEntity<Api<NoticeQuestionPrepareResponse>> =
        Api.OK(
            NoticeQuestionPrepareResponse.from(
                noticeQuestionUseCase.prepare(request.toCommand(user.userId)),
                properties.answerTemperature,
                properties.answerMaxOutputTokens,
                withDebug = true,
            ),
            "질문을 준비했습니다."
        )

    @PostMapping("/{questionId}/finish")
    override fun finish(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable questionId: String,
        @RequestBody @Valid request: NoticeQuestionFinishRequest,
    ): ResponseEntity<Api<NoticeQuestionFinishResponse>> =
        Api.OK(
            NoticeQuestionFinishResponse.from(
                noticeQuestionUseCase.finish(request.toCommand(QuestionAsker.Admin(user.userId), parseQuestionId(questionId)))
            ),
            "답변을 저장했습니다."
        )

    @GetMapping("/usage")
    override fun getUsage(@AuthenticationPrincipal user: AuthenticatedUser): ResponseEntity<Api<NoticeQuestionQuotaResponse>> =
        Api.OK(NoticeQuestionQuotaResponse.from(noticeQuestionUseCase.getQuota(QuestionAsker.Admin(user.userId))), "질문 사용량 조회 성공")

    @PatchMapping("/{questionId}/feedback")
    override fun giveFeedback(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable questionId: String,
        @RequestBody @Valid request: NoticeQuestionFeedbackRequest,
    ): ResponseEntity<Api<Boolean>> =
        Api.OK(
            noticeQuestionUseCase.giveFeedback(
                QuestionAsker.Admin(user.userId), parseQuestionId(questionId), requireNotNull(request.verdict), request.memo,
            ),
            "판정을 남겼습니다."
        )

}
