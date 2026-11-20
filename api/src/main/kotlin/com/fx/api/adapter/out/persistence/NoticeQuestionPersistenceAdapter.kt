package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.NoticeChunkSearchRepository
import com.fx.api.adapter.out.persistence.repository.NoticeQuestionRepository
import com.fx.api.application.port.out.question.NoticeQuestionPersistencePort
import com.fx.api.domain.question.NoticeChunkHit
import com.fx.api.domain.question.NoticeQuestion
import com.fx.api.domain.question.NoticeSearchRanking
import com.fx.api.exception.QuestionException
import com.fx.api.exception.errorcode.QuestionErrorCode
import com.fx.common.annotation.PersistenceAdapter
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate
import java.time.LocalDateTime

@PersistenceAdapter
class NoticeQuestionPersistenceAdapter(
    private val noticeChunkSearchRepository: NoticeChunkSearchRepository,
    private val noticeQuestionRepository: NoticeQuestionRepository,
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

    override fun getQuestion(questionId: Long): NoticeQuestion =
        noticeQuestionRepository.findByIdOrNull(questionId)
            ?: throw QuestionException(QuestionErrorCode.QUESTION_NOT_FOUND)

}
