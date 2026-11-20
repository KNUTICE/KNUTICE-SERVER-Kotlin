package com.fx.api.application.port.out.question

import com.fx.api.domain.question.NoticeChunkHit
import com.fx.api.domain.question.NoticeQuestion
import com.fx.api.domain.question.NoticeSearchRanking
import com.fx.api.exception.QuestionException
import java.time.LocalDate
import java.time.LocalDateTime

/** 질문 임베딩. 공지 청크와 같은 모델 · 차원으로 만들고 길이 1 로 정규화해 돌려준다. */
interface QuestionEmbeddingPort {

    /** `notice_chunk.embedding_model` 과 같은 표기 (예: gemini-embedding-2@768). 같은 값의 청크끼리만 비교한다 */
    val modelTag: String

    /** @throws QuestionException 한도 · 장애 (AI_RATE_LIMITED · AI_UNAVAILABLE) */
    fun embed(text: String): FloatArray

}

/** 근거 공지로 답변을 만드는 모델 */
interface AnswerGenerationPort {

    val model: String

    /** @throws QuestionException 한도 · 장애 (AI_RATE_LIMITED · AI_UNAVAILABLE) */
    fun generate(systemPrompt: String, userPrompt: String): GeneratedAnswer

}

data class GeneratedAnswer(
    val text: String,
    val inputTokens: Int?,
    val outputTokens: Int?,
)

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

    /** [from] 이후에 남긴 질문 수 */
    fun countSince(from: LocalDateTime): Long

    /** @throws QuestionException 기록이 없을 때 (QUESTION_NOT_FOUND) */
    fun getQuestion(questionId: Long): NoticeQuestion

}
