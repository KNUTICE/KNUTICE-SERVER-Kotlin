package com.fx.api.adapter.`in`.web.question.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fx.api.domain.question.FinishedQuestion
import com.fx.api.domain.question.NoticeChunkHit
import com.fx.api.domain.question.NoticeQuestionStatus
import com.fx.api.domain.question.NoticeSourceInfo
import com.fx.api.domain.question.PreparedQuestion
import com.fx.api.domain.question.QuestionQuota
import com.fx.api.domain.question.QuestionWindow
import com.fx.common.domain.notice.NoticeChunkType
import java.time.LocalDate
import java.time.LocalDateTime

/** 화면에 보여 줄 근거 공지 */
data class NoticeQuestionSourceResponse(
    /** 답변의 [n] 번호 */
    val n: Int,
    val nttId: Long,
    val title: String,
    val boardName: String?,
    val registrationDate: LocalDate,
    val contentUrl: String,
) {
    companion object {
        fun from(source: NoticeSourceInfo): NoticeQuestionSourceResponse =
            NoticeQuestionSourceResponse(source.n, source.nttId, source.title, source.boardName, source.registrationDate, source.contentUrl)
    }
}

/** 오늘 질문 사용량 (시각은 한국시간) */
data class NoticeQuestionQuotaResponse(
    val used: Long,
    val limit: Int,
    val resetsAt: LocalDateTime,
) {
    companion object {
        fun from(quota: QuestionQuota): NoticeQuestionQuotaResponse =
            NoticeQuestionQuotaResponse(used = quota.used, limit = quota.limit, resetsAt = quota.resetsAt)
    }
}

/**
 * 질문 준비 응답. status 가 PREPARED 면 [prompt] 로 사용자 키를 써서 AI 를 부르고, 받은 답을 [finishToken] 과 함께 finish 로 보낸다.
 * NOT_FOUND 면 [prompt] · [finishToken] 이 없고 AI 를 부를 필요가 없다 ([sources] 는 비슷한 공지).
 */
data class NoticeQuestionPrepareResponse(
    /** TSID 를 문자열로 내보낸다 (JS 숫자 정밀도 한계) */
    val questionId: String,
    val status: NoticeQuestionStatus,
    val sources: List<NoticeQuestionSourceResponse>,
    val prompt: Prompt?,
    val finishToken: String?,
    /** 이 시각(한국시간)까지 finish 해야 한다 */
    val finishExpiresAt: LocalDateTime?,
    val quota: NoticeQuestionQuotaResponse,
    /** 관리자 응답에만 있다 */
    @field:JsonInclude(JsonInclude.Include.NON_NULL)
    val debug: Debug? = null,
) {

    /**
     * AI 에 그대로 넘길 메시지와 권장 설정.
     * 팩트챗(OpenAI 형식): `{"model", "messages", "temperature", "max_tokens"}` 로 POST /chat/completions/
     * Gemini: system → `systemInstruction`, user → `contents[0]`, `generationConfig {temperature, maxOutputTokens}`
     */
    data class Prompt(
        val messages: List<Message>,
        val temperature: Double,
        val maxOutputTokens: Int,
    )

    data class Message(
        /** system · user */
        val role: String,
        val content: String,
    )

    data class Debug(
        val decayPerYear: Double,
        val titlePenalty: Double,
        val minSimilarity: Double,
        val elapsedMs: Int,
        /** 점수 순 검색 결과 (청크 단위) */
        val retrieved: List<Hit>,
    )

    data class Hit(
        val rank: Int,
        val nttId: Long,
        val title: String,
        val boardName: String?,
        val registrationDate: LocalDate,
        val chunkIndex: Int,
        val chunkType: NoticeChunkType,
        val similarity: Double,
        val score: Double,
        /** 근거로 쓴 공지면 근거 번호, 아니면 null */
        val sourceN: Int?,
        /** 청크 본문 앞부분 (제목 줄 제외) */
        val snippet: String,
    )

    companion object {

        private const val SNIPPET_LENGTH = 160

        fun from(result: PreparedQuestion, temperature: Double, maxOutputTokens: Int, withDebug: Boolean): NoticeQuestionPrepareResponse {
            val sourceNumbers = result.sources.associate {
                it.noticeId to it.n
            }
            return NoticeQuestionPrepareResponse(
                questionId = result.questionId.toString(),
                status = result.status,
                sources = result.sources.map {
                    NoticeQuestionSourceResponse.from(it.toInfo())
                },
                prompt = result.messages?.let { messages ->
                    Prompt(
                        messages = messages.map {
                            Message(it.role, it.content)
                        },
                        temperature = temperature,
                        maxOutputTokens = maxOutputTokens,
                    )
                },
                finishToken = result.finishToken,
                finishExpiresAt = result.finishExpiresAt?.let {
                    LocalDateTime.ofInstant(it, QuestionWindow.ZONE)
                },
                quota = NoticeQuestionQuotaResponse.from(result.quota),
                debug = if (withDebug) {
                    Debug(
                        decayPerYear = result.ranking.decayPerYear,
                        titlePenalty = result.ranking.titlePenalty,
                        minSimilarity = result.minSimilarity,
                        elapsedMs = result.elapsedMs,
                        retrieved = result.retrieved.mapIndexed { index, hit ->
                            toHit(index + 1, hit, sourceNumbers[hit.noticeId])
                        },
                    )
                } else {
                    null
                },
            )
        }

        private fun toHit(rank: Int, hit: NoticeChunkHit, sourceN: Int?): Hit =
            Hit(
                rank = rank,
                nttId = hit.nttId,
                title = hit.title,
                boardName = hit.boardName,
                registrationDate = hit.registrationDate,
                chunkIndex = hit.chunkIndex,
                chunkType = hit.chunkType,
                similarity = hit.similarity,
                score = hit.score,
                sourceN = sourceN,
                snippet = hit.body.replace('\n', ' ').take(SNIPPET_LENGTH),
            )
    }

}

/** 답변 저장 응답 */
data class NoticeQuestionFinishResponse(
    val questionId: String,
    /** 없는 근거 번호를 지운 답변. 화면은 이 글을 보여 준다 */
    val answer: String,
    /** 답변에 남은 근거 번호 */
    val citedNumbers: List<Int>,
    /** 답변이 쓴 근거 공지 */
    val sources: List<NoticeQuestionSourceResponse>,
) {
    companion object {
        fun from(result: FinishedQuestion): NoticeQuestionFinishResponse =
            NoticeQuestionFinishResponse(
                questionId = result.questionId.toString(),
                answer = result.answer,
                citedNumbers = result.citedNumbers,
                sources = result.sources.map(NoticeQuestionSourceResponse::from),
            )
    }
}
