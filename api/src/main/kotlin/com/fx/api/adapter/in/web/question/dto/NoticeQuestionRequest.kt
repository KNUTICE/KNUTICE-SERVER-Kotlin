package com.fx.api.adapter.`in`.web.question.dto

import com.fx.api.application.port.`in`.question.dto.FinishQuestionCommand
import com.fx.api.application.port.`in`.question.dto.PrepareQuestionCommand
import com.fx.api.application.port.`in`.question.dto.QuestionAsker
import com.fx.api.domain.question.NOTICE_QUESTION_MEMO_MAX_LENGTH
import com.fx.api.domain.question.NoticeAnswerProvider
import com.fx.api.domain.question.NoticeQuestionVerdict
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

private const val QUESTION_MIN_LENGTH = 2
private const val QUESTION_MAX_LENGTH = 200
private val WHITESPACE = Regex("\\s+")

private fun normalize(question: String) =
    question.trim().replace(WHITESPACE, " ")

/** 앱 질문 준비. 검색 설정은 서버 기본값만 쓴다 */
data class NoticeQuestionPrepareRequest(

    @field:NotBlank
    @field:Size(min = QUESTION_MIN_LENGTH, max = QUESTION_MAX_LENGTH)
    val question: String,
) {
    fun toCommand(fcmToken: String): PrepareQuestionCommand =
        PrepareQuestionCommand(asker = QuestionAsker.App(fcmToken), question = normalize(question))
}

/** 관리자 질문 준비. 검색 설정을 비우면 서버 기본값(`notice-question.*`)을 쓴다 */
data class NoticeQuestionAdminPrepareRequest(

    @field:NotBlank
    @field:Size(min = QUESTION_MIN_LENGTH, max = QUESTION_MAX_LENGTH)
    val question: String,

    /** 공지가 올라온 지 1년마다 빼는 점수 (0 이면 유사도만) */
    @field:DecimalMin("0.0")
    @field:DecimalMax("0.2")
    val decayPerYear: Double? = null,

    /** 제목만 있는 청크에서 빼는 점수 */
    @field:DecimalMin("0.0")
    @field:DecimalMax("0.2")
    val titlePenalty: Double? = null,

    /** 근거 공지 수 */
    @field:Min(1)
    @field:Max(10)
    val topNotices: Int? = null,
) {
    fun toCommand(userId: String): PrepareQuestionCommand =
        PrepareQuestionCommand(
            asker = QuestionAsker.Admin(userId),
            question = normalize(question),
            decayPerYear = decayPerYear,
            titlePenalty = titlePenalty,
            topNotices = topNotices,
        )
}

/**
 * 사용자 키로 받은 답변 저장. 키는 보내지 않는다 (서버는 키를 받지도 저장하지도 않는다).
 *
 * @property answer AI 가 준 답변 그대로 (근거 번호 [n] 포함). 서버가 없는 번호를 지우고 길이를 자른다
 * @property finishToken prepare 응답의 finishToken
 */
data class NoticeQuestionFinishRequest(

    @field:NotBlank
    @field:Size(max = 20_000)
    val answer: String,

    @field:NotNull
    val provider: NoticeAnswerProvider?,

    @field:NotBlank
    @field:Size(max = 100)
    val model: String,

    @field:NotBlank
    @field:Size(max = 200)
    val finishToken: String,

    @field:Min(0)
    val inputTokens: Int? = null,

    @field:Min(0)
    val outputTokens: Int? = null,
) {
    fun toCommand(asker: QuestionAsker, questionId: Long): FinishQuestionCommand =
        FinishQuestionCommand(
            asker = asker,
            questionId = questionId,
            answer = answer,
            provider = requireNotNull(provider),
            model = model,
            finishToken = finishToken,
            inputTokens = inputTokens,
            outputTokens = outputTokens,
        )
}

data class NoticeQuestionFeedbackRequest(

    @field:NotNull
    val verdict: NoticeQuestionVerdict?,

    @field:Size(max = NOTICE_QUESTION_MEMO_MAX_LENGTH)
    val memo: String? = null,
)
