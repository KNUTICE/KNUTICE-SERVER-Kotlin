package com.fx.api.application.port.out.question

import com.fx.api.domain.question.NoticeChunkHit
import com.fx.api.domain.question.NoticeQuestion
import com.fx.api.domain.question.NoticeQuestionAsker
import com.fx.api.domain.question.NoticeQuestionStatus
import com.fx.api.domain.question.NoticeSearchRanking
import com.fx.api.exception.QuestionException
import com.fx.common.domain.notice.Notice
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 질문 임베딩 (서버 무료 키). 공지 청크와 같은 모델 · 차원으로 만들고 길이 1 로 정규화해 돌려준다.
 * 답변 생성은 서버에 없다 (사용자 개인 키로 앱 · 웹이 직접 부른다).
 */
interface QuestionEmbeddingPort {

    /** `notice_chunk.embedding_model` 과 같은 표기 (예: gemini-embedding-2@768). 같은 값의 청크끼리만 비교한다 */
    val modelTag: String

    /** @throws QuestionException 한도 · 장애 (AI_RATE_LIMITED · AI_UNAVAILABLE) */
    fun embed(text: String): FloatArray

}

interface NoticeQuestionPersistencePort {

    /** 점수(유사도 − 감점)가 높은 청크부터 [limit] 개를 공지 정보와 함께 읽는다. */
    fun searchChunks(
        embedding: FloatArray,
        embeddingModel: String,
        ranking: NoticeSearchRanking,
        today: LocalDate,
        limit: Int,
    ): List<NoticeChunkHit>

    fun save(noticeQuestion: NoticeQuestion): NoticeQuestion

    /** [from] 이후 서버 전체 질문 수 */
    fun countSince(from: LocalDateTime): Long

    /** [from] 이후 [askedBy] 쪽 질문 수 */
    fun countByAskedBySince(askedBy: NoticeQuestionAsker, from: LocalDateTime): Long

    /** [from] 이후 한 사람(기기 · 관리자)의 질문 중 [excludedStatus] 가 아닌 것의 수 */
    fun countByAskerSince(
        askedBy: NoticeQuestionAsker,
        askerKey: String,
        from: LocalDateTime,
        excludedStatus: NoticeQuestionStatus,
    ): Long

    /** @throws QuestionException 기록이 없을 때 (QUESTION_NOT_FOUND) */
    fun getQuestion(questionId: Long): NoticeQuestion

    fun findNotices(nttIds: Collection<Long>): List<Notice>

}
