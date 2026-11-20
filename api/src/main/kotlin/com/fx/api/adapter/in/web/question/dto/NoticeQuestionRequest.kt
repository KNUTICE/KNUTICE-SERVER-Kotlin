package com.fx.api.adapter.`in`.web.question.dto

import com.fx.api.application.port.`in`.question.dto.NoticeQuestionCommand
import com.fx.api.domain.question.NOTICE_QUESTION_MEMO_MAX_LENGTH
import com.fx.api.domain.question.NoticeQuestionVerdict
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * 공지 질문. 검색 설정을 비우면 서버 기본값(`notice-question.*`)을 쓴다.
 */
data class NoticeQuestionRequest(

    @field:NotBlank
    @field:Size(min = 2, max = QUESTION_MAX_LENGTH)
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

    /** false 면 검색만 하고 답변 모델을 부르지 않는다 (무료 한도 아끼기) */
    val generateAnswer: Boolean = true,
) {

    fun toCommand(askerKey: String): NoticeQuestionCommand =
        NoticeQuestionCommand(
            askerKey = askerKey,
            question = question.trim().replace(WHITESPACE, " "),
            decayPerYear = decayPerYear,
            titlePenalty = titlePenalty,
            topNotices = topNotices,
            generateAnswer = generateAnswer,
        )

    companion object {
        const val QUESTION_MAX_LENGTH = 200
        private val WHITESPACE = Regex("\\s+")
    }

}

data class NoticeQuestionFeedbackRequest(

    @field:NotNull
    val verdict: NoticeQuestionVerdict?,

    @field:Size(max = NOTICE_QUESTION_MEMO_MAX_LENGTH)
    val memo: String? = null,
)
