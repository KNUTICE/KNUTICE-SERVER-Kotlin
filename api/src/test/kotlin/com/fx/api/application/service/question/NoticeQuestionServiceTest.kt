package com.fx.api.application.service.question

import com.fx.api.application.port.`in`.question.dto.NoticeQuestionCommand
import com.fx.api.application.port.out.question.AnswerGenerationPort
import com.fx.api.application.port.out.question.GeneratedAnswer
import com.fx.api.application.port.out.question.NoticeQuestionPersistencePort
import com.fx.api.application.port.out.question.QuestionEmbeddingPort
import com.fx.api.config.question.NoticeQuestionProperties
import com.fx.api.domain.question.NoticeChunkHit
import com.fx.api.domain.question.NoticeQuestion
import com.fx.api.domain.question.NoticeQuestionStatus
import com.fx.api.domain.question.NoticeQuestionVerdict
import com.fx.api.domain.question.NoticeSearchRanking
import com.fx.api.exception.QuestionException
import com.fx.api.exception.errorcode.QuestionErrorCode
import com.fx.common.domain.notice.NoticeChunkType
import com.fx.persistence.withId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class NoticeQuestionServiceTest : BehaviorSpec({

    val embeddingPort = mockk<QuestionEmbeddingPort>()
    val answerPort = mockk<AnswerGenerationPort>()
    val persistencePort = mockk<NoticeQuestionPersistencePort>()
    // 2026-11-19 11:00 KST → 하루 한도는 11/18 17:00 부터 센다
    val clock = Clock.fixed(Instant.parse("2026-11-19T02:00:00Z"), ZoneId.of("UTC"))
    val properties = NoticeQuestionProperties(dailyLimit = 3, minSimilarity = 0.6, topNotices = 2, chunksPerNotice = 2)
    val service = NoticeQuestionService(properties, embeddingPort, answerPort, persistencePort, clock)
    val windowStart = LocalDateTime.of(2026, 11, 18, 17, 0)
    val vector = FloatArray(768) {
        if (it == 0) 1f else 0f
    }

    fun hit(noticeId: Long, chunkIndex: Int, similarity: Double) =
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
            score = similarity - 0.01,
        )

    fun reset() {
        clearMocks(embeddingPort, answerPort, persistencePort)
        every {
            embeddingPort.modelTag
        } returns "gemini-embedding-2@768"
        every {
            answerPort.model
        } returns "gemini-3.5-flash-lite"
        every {
            embeddingPort.embed(any())
        } returns vector
        every {
            persistencePort.save(any())
        } answers {
            firstArg<NoticeQuestion>().withId(77)
        }
    }

    val command = NoticeQuestionCommand(askerKey = "10", question = "동계 계절학기 수강신청 언제야?")

    Given("하루 한도를 다 쓴 상태에서") {
        reset()
        every {
            persistencePort.countSince(windowStart)
        } returns 3

        Then("Gemini 를 부르지 않고 한도 초과로 거절한다") {
            val e = shouldThrow<QuestionException> {
                service.ask(command)
            }
            e.baseErrorCode shouldBe QuestionErrorCode.DAILY_LIMIT_EXCEEDED
            verify(exactly = 0) {
                embeddingPort.embed(any())
            }
        }
    }

    Given("근거로 쓸 만한 공지가 있으면") {
        reset()
        every {
            persistencePort.countSince(windowStart)
        } returns 1
        val ranking = slot<NoticeSearchRanking>()
        every {
            persistencePort.searchChunks(vector, "gemini-embedding-2@768", capture(ranking), LocalDate.of(2026, 11, 19), 150)
        } returns listOf(hit(1, 1, 0.75), hit(2, 0, 0.70), hit(1, 0, 0.69), hit(3, 0, 0.65))
        every {
            answerPort.generate(any(), any())
        } returns GeneratedAnswer("1차 수강신청은 11월 17일부터예요. [1]", 3120, 96)

        val result = service.ask(command.copy(decayPerYear = 0.5))

        Then("감점은 상한으로 줄이고, 공지 단위 근거로 답변을 만든다") {
            ranking.captured shouldBe NoticeSearchRanking(decayPerYear = 0.2, titlePenalty = 0.0)
            result.status shouldBe NoticeQuestionStatus.ANSWERED
            result.answer shouldBe "1차 수강신청은 11월 17일부터예요. [1]"
            result.sources.map {
                it.nttId
            } shouldContainExactly listOf(1001L, 1002L)
            result.questionId shouldBe 77L
            result.quota.used shouldBe 2L
            result.quota.resetsAt shouldBe LocalDateTime.of(2026, 11, 19, 17, 0)
        }

        Then("질문 · 근거 · 토큰을 기록으로 남긴다") {
            verify(exactly = 1) {
                persistencePort.save(match {
                    it.status == NoticeQuestionStatus.ANSWERED &&
                        it.sourceNttIds == listOf(1001L, 1002L) &&
                        it.topSimilarity == 0.75 &&
                        it.answerModel == "gemini-3.5-flash-lite" &&
                        it.inputTokens == 3120 &&
                        it.askerKey == "10"
                })
            }
        }
    }

    Given("가장 비슷한 공지도 기준보다 낮으면") {
        reset()
        every {
            persistencePort.countSince(windowStart)
        } returns 0
        every {
            persistencePort.searchChunks(any(), any(), any(), any(), any())
        } returns listOf(hit(1, 0, 0.55))

        val result = service.ask(command)

        Then("답변 모델을 부르지 않고 찾지 못함으로 남긴다") {
            result.status shouldBe NoticeQuestionStatus.NOT_FOUND
            result.answer shouldBe null
            result.sources.size shouldBe 1
            verify(exactly = 0) {
                answerPort.generate(any(), any())
            }
        }
    }

    Given("답변 만들기를 끄면") {
        reset()
        every {
            persistencePort.countSince(windowStart)
        } returns 0
        every {
            persistencePort.searchChunks(any(), any(), any(), any(), any())
        } returns listOf(hit(1, 0, 0.8))

        Then("검색만 한다") {
            service.ask(command.copy(generateAnswer = false)).status shouldBe NoticeQuestionStatus.SEARCH_ONLY
            verify(exactly = 0) {
                answerPort.generate(any(), any())
            }
        }
    }

    Given("Gemini 가 호출 한도를 알려 오면") {
        reset()
        every {
            persistencePort.countSince(windowStart)
        } returns 0
        every {
            embeddingPort.embed(any())
        } throws QuestionException(QuestionErrorCode.AI_RATE_LIMITED)

        Then("기록을 남기지 않고 그대로 알린다 (한도에 세지 않는다)") {
            shouldThrow<QuestionException> {
                service.ask(command)
            }.baseErrorCode shouldBe QuestionErrorCode.AI_RATE_LIMITED
            verify(exactly = 0) {
                persistencePort.save(any())
            }
        }
    }

    Given("판정을 남기면") {
        reset()
        val question = NoticeQuestion(
            askedBy = com.fx.api.domain.question.NoticeQuestionAsker.ADMIN, askerKey = "10", question = "q",
            status = NoticeQuestionStatus.ANSWERED, answer = "a", sourceNttIds = listOf(1), topSimilarity = 0.7,
            decayPerYear = 0.04, titlePenalty = 0.0, answerModel = "m", inputTokens = 1, outputTokens = 1, elapsedMs = 1,
        )
        every {
            persistencePort.getQuestion(77)
        } returns question

        Then("기록에 판정과 메모가 들어간다") {
            service.giveFeedback(77, NoticeQuestionVerdict.GOOD, "맞음") shouldBe true
            question.verdict shouldBe NoticeQuestionVerdict.GOOD
            question.memo shouldBe "맞음"
        }
    }

    Given("사용량을 물으면") {
        reset()
        every {
            persistencePort.countSince(windowStart)
        } returns 12

        Then("한국시간 17시 기준 구간의 질문 수를 센다") {
            service.getQuota().used shouldBe 12L
            service.getQuota().limit shouldBe 3
        }
    }

})
