package com.fx.api.adapter.`in`.web.question.dto

import com.fx.api.domain.question.NoticeAnswer
import com.fx.api.domain.question.NoticeChunkHit
import com.fx.api.domain.question.NoticeQuestionStatus
import com.fx.api.domain.question.NoticeSource
import com.fx.api.domain.question.QuestionQuota
import com.fx.common.domain.notice.NoticeChunkType
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 공지 질문 응답.
 *
 * [answer] · [sources] 는 학생 화면에도 그대로 보여 줄 수 있는 값이고, [debug] 는 관리자 화면만 보는 검색 정보다.
 * 나중에 학생용 API 를 열면 같은 서비스 결과에서 [debug] 만 빼고 돌려준다.
 */
data class NoticeQuestionResponse(
    /** TSID 를 문자열로 내보낸다 (JS 숫자 정밀도 한계) */
    val questionId: String,
    val status: NoticeQuestionStatus,
    /** 근거 번호를 `[1]` 처럼 붙인 답변. 근거가 약하거나 답변을 만들지 않았으면 null */
    val answer: String?,
    val sources: List<Source>,
    val debug: Debug,
    val usage: Usage,
    val quota: Quota,
) {

    data class Source(
        val n: Int,
        val nttId: Long,
        val title: String,
        val boardName: String?,
        val registrationDate: LocalDate,
        val contentUrl: String,
    )

    data class Debug(
        val decayPerYear: Double,
        val titlePenalty: Double,
        val minSimilarity: Double,
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

    data class Usage(
        val embedCalls: Int,
        val answerModel: String?,
        val answerInputTokens: Int?,
        val answerOutputTokens: Int?,
        val elapsedMs: Int,
    )

    data class Quota(
        val used: Long,
        val limit: Int,
        /** 다음 초기화 시각 (한국시간) */
        val resetsAt: LocalDateTime,
    ) {
        companion object {
            fun from(quota: QuestionQuota): Quota =
                Quota(used = quota.used, limit = quota.limit, resetsAt = quota.resetsAt)
        }
    }

    companion object {

        private const val SNIPPET_LENGTH = 160

        fun from(result: NoticeAnswer): NoticeQuestionResponse {
            val sourceNumbers = result.sources.associate {
                it.noticeId to it.n
            }
            return NoticeQuestionResponse(
                questionId = result.questionId.toString(),
                status = result.status,
                answer = result.answer,
                sources = result.sources.map(::toSource),
                debug = Debug(
                    decayPerYear = result.ranking.decayPerYear,
                    titlePenalty = result.ranking.titlePenalty,
                    minSimilarity = result.minSimilarity,
                    retrieved = result.retrieved.mapIndexed { index, hit ->
                        toHit(index + 1, hit, sourceNumbers[hit.noticeId])
                    },
                ),
                usage = Usage(
                    embedCalls = 1,
                    answerModel = result.answerModel,
                    answerInputTokens = result.inputTokens,
                    answerOutputTokens = result.outputTokens,
                    elapsedMs = result.elapsedMs,
                ),
                quota = Quota.from(result.quota),
            )
        }

        private fun toSource(source: NoticeSource): Source =
            Source(
                n = source.n,
                nttId = source.nttId,
                title = source.title,
                boardName = source.boardName,
                registrationDate = source.registrationDate,
                contentUrl = source.contentUrl,
            )

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

/** 오늘 질문 사용량 */
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
