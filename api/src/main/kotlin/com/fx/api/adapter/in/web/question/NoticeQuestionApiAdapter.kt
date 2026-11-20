package com.fx.api.adapter.`in`.web.question

import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionFeedbackRequest
import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionQuotaResponse
import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionRequest
import com.fx.api.adapter.`in`.web.question.dto.NoticeQuestionResponse
import com.fx.api.application.port.`in`.question.NoticeQuestionUseCase
import com.fx.api.config.security.dto.AuthenticatedUser
import com.fx.api.exception.QuestionException
import com.fx.api.exception.errorcode.QuestionErrorCode
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

@WebInputAdapter
@RequestMapping("/api/v1/notice-questions")
class NoticeQuestionApiAdapter(
    private val noticeQuestionUseCase: NoticeQuestionUseCase,
) : NoticeQuestionApiSwagger {

    @PostMapping
    override fun ask(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @RequestBody @Valid request: NoticeQuestionRequest,
    ): ResponseEntity<Api<NoticeQuestionResponse>> =
        Api.OK(
            NoticeQuestionResponse.from(noticeQuestionUseCase.ask(request.toCommand(user.userId))),
            "질문을 처리했습니다."
        )

    @GetMapping("/usage")
    override fun getUsage(): ResponseEntity<Api<NoticeQuestionQuotaResponse>> =
        Api.OK(NoticeQuestionQuotaResponse.from(noticeQuestionUseCase.getQuota()), "질문 사용량 조회 성공")

    @PatchMapping("/{questionId}/feedback")
    override fun giveFeedback(
        @PathVariable questionId: String,
        @RequestBody @Valid request: NoticeQuestionFeedbackRequest,
    ): ResponseEntity<Api<Boolean>> {
        val id = questionId.toLongOrNull()
            ?: throw QuestionException(QuestionErrorCode.QUESTION_NOT_FOUND)
        return Api.OK(
            noticeQuestionUseCase.giveFeedback(id, requireNotNull(request.verdict), request.memo),
            "판정을 남겼습니다."
        )
    }

}
