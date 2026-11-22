package com.fx.api.application.service.question

import com.fx.api.application.port.`in`.question.dto.CompareSearchCommand
import com.fx.api.application.port.out.question.NoticeQuestionPersistencePort
import com.fx.api.application.port.out.question.QuestionEmbeddingPort
import com.fx.api.config.question.NoticeQuestionProperties
import com.fx.api.domain.question.NoticeChunkHit
import com.fx.api.domain.question.NoticeSearchRanking
import com.fx.api.exception.QuestionException
import com.fx.api.exception.errorcode.QuestionErrorCode
import com.fx.common.domain.notice.NoticeChunkType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class NoticeSearchCompareServiceTest : BehaviorSpec({

    val embeddingPort = mockk<QuestionEmbeddingPort>()
    val persistencePort = mockk<NoticeQuestionPersistencePort>()
    // 2026-11-20 10:00 KST → 하루 한도는 11/20 0시부터 센다
    val clock = Clock.fixed(Instant.parse("2026-11-20T01:00:00Z"), ZoneId.of("UTC"))
    val properties = NoticeQuestionProperties(compareDailyLimit = 2)
    val today = LocalDate.of(2026, 11, 20)
    val vector = FloatArray(768) {
        if (it == 0) 1f else 0f
    }
    val similarityOnly = NoticeSearchRanking(decayPerYear = 0.0, titlePenalty = 0.0)
    val recentFirst = NoticeSearchRanking(decayPerYear = 0.04, titlePenalty = 0.0)

    fun hit(noticeId: Long, chunkIndex: Int, similarity: Double, score: Double = similarity) =
        NoticeChunkHit(
            noticeId = noticeId,
            nttId = noticeId + 1000,
            title = "공지 $noticeId",
            registrationDate = LocalDate.of(2026, 11, 10),
            contentUrl = "https://www.ut.ac.kr/$noticeId",
            chunkIndex = chunkIndex,
            chunkType = NoticeChunkType.BODY,
            content = "[학사공지사항] 공지 $noticeId\n본문 $chunkIndex",
            similarity = similarity,
            score = score,
        )

    /** 시나리오마다 새 서비스 (임베딩 캐시 · 하루 사용량이 서비스 안에 있다) */
    fun reset(): NoticeSearchCompareService {
        clearMocks(embeddingPort, persistencePort)
        every { embeddingPort.modelTag } returns "gemini-embedding-2@768"
        every { embeddingPort.embed(any()) } returns vector
        // A: 옛 공지(1)가 유사도로 앞서고, B: 감점 뒤 최근 공지(2)가 앞선다
        every { persistencePort.searchChunks(vector, any(), similarityOnly, any(), any()) } returns listOf(
            hit(1, 0, 0.80), hit(2, 1, 0.75), hit(2, 0, 0.70), hit(3, 0, 0.60),
        )
        every { persistencePort.searchChunks(vector, any(), recentFirst, any(), any()) } returns listOf(
            hit(2, 1, 0.75, 0.74), hit(1, 0, 0.80, 0.72), hit(2, 0, 0.70, 0.69),
        )
        return NoticeSearchCompareService(properties, embeddingPort, persistencePort, clock)
    }

    Given("처음 보는 질문을 비교하면") {
        val service = reset()
        val result = service.compare(CompareSearchCommand(question = "계절학기 언제야?", decayPerYear = 0.04, titlePenalty = 0.0))

        Then("임베딩은 한 번, 검색은 A(유사도만) · B(검색 설정) 두 번 한다") {
            verify(exactly = 1) { embeddingPort.embed("계절학기 언제야?") }
            verify(exactly = 1) { persistencePort.searchChunks(vector, "gemini-embedding-2@768", similarityOnly, today, properties.candidateChunks) }
            verify(exactly = 1) { persistencePort.searchChunks(vector, "gemini-embedding-2@768", recentFirst, today, properties.candidateChunks) }
        }

        Then("공지 단위로 묶고, 공지마다 점수가 가장 높은 청크 하나만 남긴다") {
            result.baseline.map { it.noticeId } shouldContainExactly listOf(1L, 2L, 3L)
            result.ranked.map { it.noticeId } shouldContainExactly listOf(2L, 1L)
            result.ranked.first().hits.map { it.chunkIndex } shouldContainExactly listOf(1)
        }

        Then("새로 임베딩했으므로 캐시를 쓰지 않았고 사용량이 1 이다") {
            result.embeddingCached shouldBe false
            result.usage.used shouldBe 1
            result.usage.limit shouldBe 2
            result.usage.resetsAt shouldBe LocalDateTime.of(2026, 11, 21, 0, 0)
        }
    }

    Given("검색 설정만 바꿔 같은 질문을 다시 비교하면") {
        val service = reset()
        service.compare(CompareSearchCommand(question = "계절학기 언제야?", decayPerYear = 0.04))
        val again = service.compare(CompareSearchCommand(question = "계절학기 언제야?", decayPerYear = 0.0))

        Then("메모리의 임베딩을 다시 써서 서버 키를 한 번만 쓴다") {
            again.embeddingCached shouldBe true
            again.usage.used shouldBe 1
            verify(exactly = 1) { embeddingPort.embed(any()) }
        }
    }

    Given("하루 새 질문 수를 다 쓰면") {
        val service = reset()
        service.compare(CompareSearchCommand(question = "첫 질문"))
        service.compare(CompareSearchCommand(question = "둘째 질문"))

        Then("새 질문은 임베딩하지 않고 COMPARE_DAILY_LIMIT_EXCEEDED 로 막는다") {
            shouldThrow<QuestionException> {
                service.compare(CompareSearchCommand(question = "셋째 질문"))
            }.baseErrorCode shouldBe QuestionErrorCode.COMPARE_DAILY_LIMIT_EXCEEDED
            verify(exactly = 0) { embeddingPort.embed("셋째 질문") }
        }

        Then("이미 비교한 질문은 계속 비교할 수 있다") {
            service.compare(CompareSearchCommand(question = "첫 질문")).embeddingCached shouldBe true
        }
    }

    Given("임베딩이 실패하면") {
        val service = reset()
        every { embeddingPort.embed("실패 질문") } throws QuestionException(QuestionErrorCode.AI_RATE_LIMITED)

        Then("예외를 그대로 던지고 사용량에 세지 않는다") {
            shouldThrow<QuestionException> {
                service.compare(CompareSearchCommand(question = "실패 질문"))
            }.baseErrorCode shouldBe QuestionErrorCode.AI_RATE_LIMITED
            service.getUsage().used shouldBe 0
        }
    }

})
