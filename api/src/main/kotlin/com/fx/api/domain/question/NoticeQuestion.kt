package com.fx.api.domain.question

import com.fx.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table

/** 질문 최대 길이 — 요청 검증(200자)보다 넉넉한 컬럼 길이 */
const val NOTICE_QUESTION_MAX_LENGTH = 500

/** 관리자 메모 최대 길이 */
const val NOTICE_QUESTION_MEMO_MAX_LENGTH = 500

/**
 * 공지 질문(RAG) 기록. 답변 품질을 점검하고, 하루 질문 한도를 셀 때 쓴다.
 *
 * - 근거 공지는 외래 키 없이 nttId 목록으로 남긴다 (공지는 하드 삭제).
 * - 질문에 개인정보가 들어올 수 있으므로 오래 보관하지 않는다 (보관 기간 정리는 다음 작업).
 */
@Entity
@Table(
    name = "notice_question",
    indexes = [
        // 하루 한도 : `WHERE created_at >= ?`
        Index(name = "idx_notice_question_created_at", columnList = "created_at"),
    ],
)
class NoticeQuestion(
    askedBy: NoticeQuestionAsker,
    askerKey: String,
    question: String,
    status: NoticeQuestionStatus,
    answer: String?,
    sourceNttIds: List<Long>,
    topSimilarity: Double?,
    decayPerYear: Double,
    titlePenalty: Double,
    answerModel: String?,
    inputTokens: Int?,
    outputTokens: Int?,
    elapsedMs: Int,
) : BaseEntity() {

    @Enumerated(EnumType.STRING)
    @Column(name = "asked_by", nullable = false, updatable = false, length = 20, comment = "ADMIN")
    val askedBy: NoticeQuestionAsker = askedBy

    @Column(name = "asker_key", nullable = false, updatable = false, length = 64, comment = "질문한 관리자 user.id")
    val askerKey: String = askerKey

    @Column(name = "question", nullable = false, updatable = false, length = NOTICE_QUESTION_MAX_LENGTH, comment = "질문")
    val question: String = question

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, updatable = false, length = 20, comment = "ANSWERED / NOT_FOUND / SEARCH_ONLY")
    val status: NoticeQuestionStatus = status

    @Column(name = "answer", nullable = true, updatable = false, columnDefinition = "TEXT", comment = "AI 답변")
    val answer: String? = answer

    @Column(name = "source_ntt_ids", nullable = true, updatable = false, length = 500, comment = "근거 공지 nttId (쉼표로 구분)")
    private val sourceNttIdsText: String? = sourceNttIds.takeIf {
        it.isNotEmpty()
    }?.joinToString(",")

    @Column(name = "top_similarity", nullable = true, updatable = false, comment = "근거 공지 중 가장 높은 코사인 유사도")
    val topSimilarity: Double? = topSimilarity

    @Column(name = "decay_per_year", nullable = false, updatable = false, comment = "공지가 올라온 지 1년마다 빼는 점수")
    val decayPerYear: Double = decayPerYear

    @Column(name = "title_penalty", nullable = false, updatable = false, comment = "제목만 있는 청크에서 빼는 점수")
    val titlePenalty: Double = titlePenalty

    @Column(name = "answer_model", nullable = true, updatable = false, length = 50, comment = "답변 모델")
    val answerModel: String? = answerModel

    @Column(name = "input_tokens", nullable = true, updatable = false, comment = "답변 입력 토큰")
    val inputTokens: Int? = inputTokens

    @Column(name = "output_tokens", nullable = true, updatable = false, comment = "답변 출력 토큰")
    val outputTokens: Int? = outputTokens

    @Column(name = "elapsed_ms", nullable = false, updatable = false, comment = "질문 처리에 걸린 시간 (ms)")
    val elapsedMs: Int = elapsedMs

    @Enumerated(EnumType.STRING)
    @Column(name = "verdict", nullable = true, length = 20, comment = "관리자 판정 GOOD / BAD")
    var verdict: NoticeQuestionVerdict? = null
        protected set

    @Column(name = "memo", nullable = true, length = NOTICE_QUESTION_MEMO_MAX_LENGTH, comment = "관리자 메모")
    var memo: String? = null
        protected set

    /** 근거 공지 nttId (근거 번호 순서) */
    val sourceNttIds: List<Long>
        get() = sourceNttIdsText?.split(",")?.mapNotNull {
            it.trim().toLongOrNull()
        }.orEmpty()

    /** 관리자 판정과 메모를 남긴다. 다시 남기면 덮어쓴다. */
    fun giveFeedback(verdict: NoticeQuestionVerdict, memo: String?) {
        this.verdict = verdict
        this.memo = memo?.trim()?.takeIf {
            it.isNotEmpty()
        }?.take(NOTICE_QUESTION_MEMO_MAX_LENGTH)
    }

}
