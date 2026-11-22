package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.NoticeChunkSearchRepository
import com.fx.api.adapter.out.persistence.repository.NoticeQuestionRepository
import com.fx.api.application.port.out.question.NoticeQuestionPersistencePort
import com.fx.api.domain.question.NoticeChunkHit
import com.fx.api.domain.question.NoticeQuestion
import com.fx.api.domain.question.NoticeQuestionAsker
import com.fx.api.domain.question.NoticeQuestionStatus
import com.fx.api.domain.question.NoticeSearchRanking
import com.fx.api.exception.QuestionException
import com.fx.api.exception.errorcode.QuestionErrorCode
import com.fx.common.annotation.PersistenceAdapter
import com.fx.common.domain.notice.Notice
import jakarta.persistence.EntityManager
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate
import java.time.LocalDateTime

@PersistenceAdapter
class NoticeQuestionPersistenceAdapter(
    private val noticeChunkSearchRepository: NoticeChunkSearchRepository,
    private val noticeQuestionRepository: NoticeQuestionRepository,
    private val entityManager: EntityManager,
) : NoticeQuestionPersistencePort {

    override fun searchChunks(
        embedding: FloatArray,
        embeddingModel: String,
        ranking: NoticeSearchRanking,
        today: LocalDate,
        limit: Int,
    ): List<NoticeChunkHit> =
        noticeChunkSearchRepository.search(embedding, embeddingModel, ranking, today, limit)

    override fun save(noticeQuestion: NoticeQuestion): NoticeQuestion =
        noticeQuestionRepository.save(noticeQuestion)

    override fun countSince(from: LocalDateTime): Long =
        noticeQuestionRepository.countByCreatedAtGreaterThanEqual(from)

    override fun countByAskedBySince(askedBy: NoticeQuestionAsker, from: LocalDateTime): Long =
        noticeQuestionRepository.countByAskedByAndCreatedAtGreaterThanEqual(askedBy, from)

    override fun countByAskerSince(
        askedBy: NoticeQuestionAsker,
        askerKey: String,
        from: LocalDateTime,
        excludedStatus: NoticeQuestionStatus,
    ): Long =
        noticeQuestionRepository.countByAskedByAndAskerKeyAndStatusNotAndCreatedAtGreaterThanEqual(askedBy, askerKey, excludedStatus, from)

    override fun getQuestion(questionId: Long): NoticeQuestion =
        noticeQuestionRepository.findByIdOrNull(questionId)
            ?: throw QuestionException(QuestionErrorCode.QUESTION_NOT_FOUND)

    override fun findNotices(nttIds: Collection<Long>): List<Notice> =
        if (nttIds.isEmpty()) {
            emptyList()
        } else {
            entityManager.createQuery("SELECT n FROM Notice n WHERE n.nttId IN :nttIds", Notice::class.java)
                .setParameter("nttIds", nttIds)
                .resultList
        }

}
