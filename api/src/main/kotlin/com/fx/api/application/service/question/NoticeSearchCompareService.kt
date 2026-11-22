package com.fx.api.application.service.question

import com.fx.api.application.port.`in`.question.NoticeSearchCompareUseCase
import com.fx.api.application.port.`in`.question.dto.CompareSearchCommand
import com.fx.api.application.port.out.question.NoticeQuestionPersistencePort
import com.fx.api.application.port.out.question.QuestionEmbeddingPort
import com.fx.api.config.question.NoticeQuestionProperties
import com.fx.api.domain.question.NoticeSearchComparison
import com.fx.api.domain.question.NoticeSearchRanking
import com.fx.api.domain.question.NoticeSource
import com.fx.api.domain.question.NoticeSourcePicker
import com.fx.api.domain.question.QuestionQuota
import com.fx.api.domain.question.QuestionWindow
import com.fx.api.exception.QuestionException
import com.fx.api.exception.errorcode.QuestionErrorCode
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 관리자 검색 비교. 질문을 한 번 임베딩해 "유사도만"(A)과 "검색 설정 적용"(B)으로 두 번 검색한다.
 *
 * - 같은 질문의 임베딩은 최근 [CACHE_SIZE] 개까지 메모리에 둔다. 검색 설정만 바꿔 다시 비교하면 서버 키를 쓰지 않는다.
 * - 새로 임베딩하는 질문은 하루 [NoticeQuestionProperties.compareDailyLimit] 개까지. 기록을 남기지 않으므로 메모리에서 센다 (재시작하면 0).
 * - 임베딩이 실패하면 센 횟수를 되돌린다.
 */
@Service
class NoticeSearchCompareService(
    private val properties: NoticeQuestionProperties,
    private val questionEmbeddingPort: QuestionEmbeddingPort,
    private val noticeQuestionPersistencePort: NoticeQuestionPersistencePort,
    private val clock: Clock,
) : NoticeSearchCompareUseCase {

    private val cache = object : LinkedHashMap<String, FloatArray>(CACHE_SIZE, LOAD_FACTOR, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, FloatArray>): Boolean =
            size > CACHE_SIZE
    }
    private var windowStart: LocalDateTime? = null
    private var embeddedToday = 0

    override fun compare(command: CompareSearchCommand): NoticeSearchComparison {
        val startedAt = System.nanoTime()
        val now = now()
        val ranking = NoticeSearchRanking(
            decayPerYear = (command.decayPerYear ?: properties.decayPerYear).coerceIn(0.0, MAX_PENALTY),
            titlePenalty = (command.titlePenalty ?: properties.titlePenalty).coerceIn(0.0, MAX_PENALTY),
        )
        val topNotices = (command.topNotices ?: DEFAULT_TOP_NOTICES).coerceIn(1, MAX_TOP_NOTICES)

        val (embedding, cached) = embeddingOf(command.question, now)
        val today = now.toLocalDate()
        val baseline = search(embedding, SIMILARITY_ONLY, today, topNotices)
        val ranked = search(embedding, ranking, today, topNotices)

        return NoticeSearchComparison(
            baseline = baseline,
            ranked = ranked,
            ranking = ranking,
            minSimilarity = properties.minSimilarity,
            embeddingCached = cached,
            elapsedMs = ((System.nanoTime() - startedAt) / NANOS_PER_MILLI).toInt(),
            usage = usage(now),
        )
    }

    override fun getUsage(): QuestionQuota =
        usage(now())

    /** 공지 단위로 묶어 [topNotices] 건. 공지마다 점수가 가장 높은 청크 하나만 남긴다 */
    private fun search(embedding: FloatArray, ranking: NoticeSearchRanking, today: LocalDate, topNotices: Int): List<NoticeSource> {
        val hits = noticeQuestionPersistencePort.searchChunks(
            embedding = embedding,
            embeddingModel = questionEmbeddingPort.modelTag,
            ranking = ranking,
            today = today,
            limit = properties.candidateChunks,
        )
        return NoticeSourcePicker.pick(hits, topNotices, chunksPerNotice = 1)
    }

    private fun embeddingOf(question: String, now: LocalDateTime): Pair<FloatArray, Boolean> {
        synchronized(this) {
            cache[question]?.let {
                return it to true
            }
            resetIfNewWindow(now)
            if (embeddedToday >= properties.compareDailyLimit) {
                throw QuestionException(QuestionErrorCode.COMPARE_DAILY_LIMIT_EXCEEDED)
            }
            embeddedToday++
        }
        val embedding = try {
            questionEmbeddingPort.embed(question)
        } catch (e: QuestionException) {
            synchronized(this) {
                embeddedToday = (embeddedToday - 1).coerceAtLeast(0)
            }
            throw e
        }
        synchronized(this) {
            cache[question] = embedding
        }
        return embedding to false
    }

    private fun usage(now: LocalDateTime): QuestionQuota =
        synchronized(this) {
            resetIfNewWindow(now)
            QuestionQuota(
                used = embeddedToday.toLong(),
                limit = properties.compareDailyLimit,
                resetsAt = QuestionWindow.nextReset(now, properties.resetHour),
            )
        }

    private fun resetIfNewWindow(now: LocalDateTime) {
        val start = QuestionWindow.start(now, properties.resetHour)
        if (windowStart != start) {
            windowStart = start
            embeddedToday = 0
        }
    }

    private fun now(): LocalDateTime =
        LocalDateTime.now(clock.withZone(QuestionWindow.ZONE))

    companion object {
        private val SIMILARITY_ONLY = NoticeSearchRanking(decayPerYear = 0.0, titlePenalty = 0.0)
        private const val DEFAULT_TOP_NOTICES = 10
        private const val MAX_TOP_NOTICES = 20
        /** 요청으로 바꿀 수 있는 감점의 상한 */
        private const val MAX_PENALTY = 0.2
        private const val CACHE_SIZE = 200
        private const val LOAD_FACTOR = 0.75f
        private const val NANOS_PER_MILLI = 1_000_000
    }

}
