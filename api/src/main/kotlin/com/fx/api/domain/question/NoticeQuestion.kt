package com.fx.api.domain.question

import com.fx.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

/** 질문 최대 길이 — 요청 검증(200자)보다 넉넉한 컬럼 길이 */
const val NOTICE_QUESTION_MAX_LENGTH = 500

/** 판정 메모 최대 길이 */
const val NOTICE_QUESTION_MEMO_MAX_LENGTH = 500

/** 답변 모델 이름 최대 길이 */
const val NOTICE_ANSWER_MODEL_MAX_LENGTH = 50

/**
 * 공지 질문(RAG) 기록. 답변 품질을 점검하고, 하루 질문 한도를 셀 때 쓴다.
 *
 * - 서버는 검색 · 프롬프트까지만 만든다 ([NoticeQuestionStatus.PREPARED]).
 *   앱 · 관리자 웹이 사용자 개인 AI 키로 답변을 받아 돌려주면 [finish] 로 저장한다.
 * - 근거 공지는 외래 키 없이 nttId 목록으로 남긴다 (공지는 하드 삭제).
 * - 질문에 개인정보가 들어올 수 있으므로 오래 보관하지 않는다 (보관 기간 정리는 다음 작업).
 */
@Entity
@Table(
    name = "notice_question",
    indexes = [
        // 서버 전체 하루 한도 : `WHERE created_at >= ?`
        Index(name = "idx_notice_question_created_at", columnList = "created_at"),
        // 기기 · 관리자별 하루 한도 : `WHERE asker_key = ? AND created_at >= ?`
        Index(name = "idx_notice_question_asker_key_created_at", columnList = "asker_key, created_at"),
    ],
)
class NoticeQuestion(
    askedBy: NoticeQuestionAsker,
    askerKey: String,
    question: String,
    status: NoticeQuestionStatus,
    sourceNttIds: List<Long>,
    topSimilarity: Double?,
    decayPerYear: Double,
    titlePenalty: Double,
    elapsedMs: Int,
) : BaseEntity() {

    init {
        require(status != NoticeQuestionStatus.ANSWERED) {
            "답변은 finish 로만 저장합니다."
        }
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "asked_by", nullable = false, updatable = false, length = 20, comment = "ADMIN / APP")
    val askedBy: NoticeQuestionAsker = askedBy

    @Column(name = "asker_key", nullable = false, updatable = false, length = 64, comment = "user.id 또는 fcmToken 의 HMAC")
    val askerKey: String = askerKey

    @Column(name = "question", nullable = false, updatable = false, length = NOTICE_QUESTION_MAX_LENGTH, comment = "질문")
    val question: String = question

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, comment = "PREPARED / ANSWERED / NOT_FOUND")
    var status: NoticeQuestionStatus = status
        protected set

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

    @Column(name = "elapsed_ms", nullable = false, updatable = false, comment = "검색 · 프롬프트 준비에 걸린 시간 (ms)")
    val elapsedMs: Int = elapsedMs

    @Column(name = "answer", nullable = true, columnDefinition = "TEXT", comment = "사용자 키로 받은 답변")
    var answer: String? = null
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "answer_provider", nullable = true, length = 20, comment = "FACTCHAT / GEMINI")
    var answerProvider: NoticeAnswerProvider? = null
        protected set

    @Column(name = "answer_model", nullable = true, length = NOTICE_ANSWER_MODEL_MAX_LENGTH, comment = "답변 모델")
    var answerModel: String? = null
        protected set

    @Column(name = "input_tokens", nullable = true, comment = "답변 입력 토큰 (앱이 알려 준 값)")
    var inputTokens: Int? = null
        protected set

    @Column(name = "output_tokens", nullable = true, comment = "답변 출력 토큰 (앱이 알려 준 값)")
    var outputTokens: Int? = null
        protected set

    @Column(name = "answered_at", nullable = true, comment = "답변 저장 시각")
    var answeredAt: LocalDateTime? = null
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "verdict", nullable = true, length = 20, comment = "판정 GOOD / BAD")
    var verdict: NoticeQuestionVerdict? = null
        protected set

    @Column(name = "memo", nullable = true, length = NOTICE_QUESTION_MEMO_MAX_LENGTH, comment = "판정 메모")
    var memo: String? = null
        protected set

    /** 근거 공지 nttId (근거 번호 순서) */
    val sourceNttIds: List<Long>
        get() = sourceNttIdsText?.split(",")?.mapNotNull {
            it.trim().toLongOrNull()
        }.orEmpty()

    /** 같은 사람(관리자 · 기기)의 질문인지 */
    fun isAskedBy(askedBy: NoticeQuestionAsker, askerKey: String): Boolean =
        this.askedBy == askedBy && this.askerKey == askerKey

    /**
     * 사용자 키로 받은 답변을 저장한다. 프롬프트를 준 질문(PREPARED)에 한 번만 저장할 수 있다.
     *
     * @return 저장했으면 true, 이미 답했거나 근거가 없던 질문이면 false
     */
    fun finish(
        answer: String,
        provider: NoticeAnswerProvider,
        model: String,
        inputTokens: Int?,
        outputTokens: Int?,
        answeredAt: LocalDateTime,
    ): Boolean {
        if (status != NoticeQuestionStatus.PREPARED) {
            return false
        }
        this.status = NoticeQuestionStatus.ANSWERED
        this.answer = answer
        this.answerProvider = provider
        this.answerModel = model.take(NOTICE_ANSWER_MODEL_MAX_LENGTH)
        this.inputTokens = inputTokens?.takeIf {
            it >= 0
        }
        this.outputTokens = outputTokens?.takeIf {
            it >= 0
        }
        this.answeredAt = answeredAt
        return true
    }

    /** 판정과 메모를 남긴다. 다시 남기면 덮어쓴다. */
    fun giveFeedback(verdict: NoticeQuestionVerdict, memo: String?) {
        this.verdict = verdict
        this.memo = memo?.trim()?.takeIf {
            it.isNotEmpty()
        }?.take(NOTICE_QUESTION_MEMO_MAX_LENGTH)
    }

}
