package com.fx.api.application.service.question

import com.fx.api.application.port.`in`.question.NoticeQuestionUseCase
import com.fx.api.application.port.`in`.question.dto.NoticeQuestionCommand
import com.fx.api.application.port.out.question.AnswerGenerationPort
import com.fx.api.application.port.out.question.NoticeQuestionPersistencePort
import com.fx.api.application.port.out.question.QuestionEmbeddingPort
import com.fx.api.config.question.NoticeQuestionProperties
import com.fx.api.domain.question.NoticeAnswer
import com.fx.api.domain.question.NoticeAnswerPrompt
import com.fx.api.domain.question.NoticeQuestion
import com.fx.api.domain.question.NoticeQuestionAsker
import com.fx.api.domain.question.NoticeQuestionStatus
import com.fx.api.domain.question.NoticeQuestionVerdict
import com.fx.api.domain.question.NoticeSearchRanking
import com.fx.api.domain.question.NoticeSourcePicker
import com.fx.api.domain.question.QuestionQuota
import com.fx.api.domain.question.QuestionWindow
import com.fx.api.exception.QuestionException
import com.fx.api.exception.errorcode.QuestionErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

/**
 * 공지 질문(RAG): 한도 확인 → 질문 임베딩 → 벡터 검색(최근 공지 가산) → 근거가 있으면 답변 → 기록.
 *
 * - Gemini 를 부르는 동안 DB 트랜잭션을 잡지 않도록 메서드 전체에 트랜잭션을 걸지 않는다 (기록 저장만 따로).
 * - 돈 · 한도가 드는 호출은 임베딩 1회와 답변 1회뿐이다. 한도는 그 전에 확인하고, 근거가 약하면 답변을 건너뛴다.
 * - 동시에 들어온 질문은 한도를 조금 넘길 수 있다 (관리자 몇 명이 쓰는 시험 화면이라 잠그지 않는다).
 */
@Service
class NoticeQuestionService(
    private val properties: NoticeQuestionProperties,
    private val questionEmbeddingPort: QuestionEmbeddingPort,
    private val answerGenerationPort: AnswerGenerationPort,
    private val noticeQuestionPersistencePort: NoticeQuestionPersistencePort,
    private val clock: Clock,
) : NoticeQuestionUseCase {

    override fun ask(command: NoticeQuestionCommand): NoticeAnswer {
        val startedAt = System.nanoTime()
        val now = now()
        val used = noticeQuestionPersistencePort.countSince(QuestionWindow.start(now, properties.resetHour))
        if (used >= properties.dailyLimit) {
            throw QuestionException(QuestionErrorCode.DAILY_LIMIT_EXCEEDED)
        }

        val ranking = NoticeSearchRanking(
            decayPerYear = (command.decayPerYear ?: properties.decayPerYear).coerceIn(0.0, MAX_PENALTY),
            titlePenalty = (command.titlePenalty ?: properties.titlePenalty).coerceIn(0.0, MAX_PENALTY),
        )
        val topNotices = (command.topNotices ?: properties.topNotices).coerceIn(1, MAX_TOP_NOTICES)

        val embedding = questionEmbeddingPort.embed(command.question)
        val hits = noticeQuestionPersistencePort.searchChunks(
            embedding = embedding,
            embeddingModel = questionEmbeddingPort.modelTag,
            ranking = ranking,
            today = now.toLocalDate(),
            limit = properties.candidateChunks,
        )
        val sources = NoticeSourcePicker.pick(hits, topNotices, properties.chunksPerNotice)
        val topSimilarity = sources.maxOfOrNull {
            it.similarity
        }
        val found = topSimilarity != null && topSimilarity >= properties.minSimilarity

        val generated = if (found && command.generateAnswer) {
            answerGenerationPort.generate(
                NoticeAnswerPrompt.SYSTEM,
                NoticeAnswerPrompt.user(command.question, sources, now),
            )
        } else {
            null
        }
        val status = when {
            !found -> NoticeQuestionStatus.NOT_FOUND
            generated == null -> NoticeQuestionStatus.SEARCH_ONLY
            else -> NoticeQuestionStatus.ANSWERED
        }
        val elapsedMs = ((System.nanoTime() - startedAt) / NANOS_PER_MILLI).toInt()

        val saved = noticeQuestionPersistencePort.save(
            NoticeQuestion(
                askedBy = NoticeQuestionAsker.ADMIN,
                askerKey = command.askerKey,
                question = command.question,
                status = status,
                answer = generated?.text,
                sourceNttIds = sources.map {
                    it.nttId
                },
                topSimilarity = topSimilarity,
                decayPerYear = ranking.decayPerYear,
                titlePenalty = ranking.titlePenalty,
                answerModel = generated?.let {
                    answerGenerationPort.model
                },
                inputTokens = generated?.inputTokens,
                outputTokens = generated?.outputTokens,
                elapsedMs = elapsedMs,
            )
        )

        return NoticeAnswer(
            questionId = requireNotNull(saved.id),
            status = status,
            answer = generated?.text,
            sources = sources,
            retrieved = hits.take(properties.retrievedLimit),
            ranking = ranking,
            minSimilarity = properties.minSimilarity,
            answerModel = saved.answerModel,
            inputTokens = generated?.inputTokens,
            outputTokens = generated?.outputTokens,
            elapsedMs = elapsedMs,
            quota = QuestionQuota(
                used = used + 1,
                limit = properties.dailyLimit,
                resetsAt = QuestionWindow.nextReset(now, properties.resetHour),
            ),
        )
    }

    override fun getQuota(): QuestionQuota {
        val now = now()
        return QuestionQuota(
            used = noticeQuestionPersistencePort.countSince(QuestionWindow.start(now, properties.resetHour)),
            limit = properties.dailyLimit,
            resetsAt = QuestionWindow.nextReset(now, properties.resetHour),
        )
    }

    @Transactional
    override fun giveFeedback(questionId: Long, verdict: NoticeQuestionVerdict, memo: String?): Boolean {
        noticeQuestionPersistencePort.getQuestion(questionId).giveFeedback(verdict, memo)
        return true
    }

    private fun now(): LocalDateTime =
        LocalDateTime.now(clock.withZone(QuestionWindow.ZONE))

    companion object {
        /** 요청으로 바꿀 수 있는 감점의 상한 */
        private const val MAX_PENALTY = 0.2
        private const val MAX_TOP_NOTICES = 10
        private const val NANOS_PER_MILLI = 1_000_000
    }

}
