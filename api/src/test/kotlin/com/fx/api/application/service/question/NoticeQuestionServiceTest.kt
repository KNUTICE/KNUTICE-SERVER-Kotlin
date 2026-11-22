package com.fx.api.application.service.question

import com.fx.api.application.port.`in`.question.dto.FinishQuestionCommand
import com.fx.api.application.port.`in`.question.dto.PrepareQuestionCommand
import com.fx.api.application.port.`in`.question.dto.QuestionAsker
import com.fx.api.application.port.out.fcmtoken.FcmTokenPersistencePort
import com.fx.api.application.port.out.question.NoticeQuestionPersistencePort
import com.fx.api.application.port.out.question.QuestionEmbeddingPort
import com.fx.api.config.question.NoticeQuestionProperties
import com.fx.api.domain.question.NoticeAnswerProvider
import com.fx.api.domain.question.NoticeChunkHit
import com.fx.api.domain.question.NoticeQuestion
import com.fx.api.domain.question.NoticeQuestionAsker
import com.fx.api.domain.question.NoticeQuestionStatus
import com.fx.api.domain.question.NoticeQuestionVerdict
import com.fx.api.domain.question.NoticeSearchRanking
import com.fx.api.exception.QuestionException
import com.fx.api.exception.errorcode.QuestionErrorCode
import com.fx.api.fixture.TopicFixture
import com.fx.common.application.port.`in`.CatalogQueryUseCase
import com.fx.common.domain.notice.Notice
import com.fx.common.domain.notice.NoticeChunkType
import com.fx.common.exception.FcmTokenException
import com.fx.persistence.withId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class NoticeQuestionServiceTest : BehaviorSpec({

    val embeddingPort = mockk<QuestionEmbeddingPort>()
    val persistencePort = mockk<NoticeQuestionPersistencePort>()
    val fcmTokenPort = mockk<FcmTokenPersistencePort>()
    val catalogQueryUseCase = mockk<CatalogQueryUseCase>()
    val signer = NoticeQuestionSigner("test-secret-at-least-32-characters-long")
    // 2026-11-20 10:00 KST → 하루 한도는 11/20 0시부터 센다
    val now = Instant.parse("2026-11-20T01:00:00Z")
    val clock = Clock.fixed(now, ZoneId.of("UTC"))
    val properties = NoticeQuestionProperties(appDailyLimit = 3, adminDailyLimit = 50, totalDailyLimit = 300, topNotices = 2, chunksPerNotice = 2)
    val service = NoticeQuestionService(properties, embeddingPort, persistencePort, fcmTokenPort, catalogQueryUseCase, signer, clock)
    val windowStart = LocalDateTime.of(2026, 11, 20, 0, 0)
    val vector = FloatArray(768) {
        if (it == 0) 1f else 0f
    }
    val device = signer.deviceKey("fcm-1")
    val app = QuestionAsker.App("fcm-1")

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

    fun record(askedBy: NoticeQuestionAsker = NoticeQuestionAsker.APP, askerKey: String = device, status: NoticeQuestionStatus = NoticeQuestionStatus.PREPARED) =
        NoticeQuestion(
            askedBy = askedBy, askerKey = askerKey, question = "q", status = status, sourceNttIds = listOf(1001, 1002),
            topSimilarity = 0.75, decayPerYear = 0.04, titlePenalty = 0.0, elapsedMs = 1,
        ).withId(77)

    fun reset() {
        clearMocks(embeddingPort, persistencePort, fcmTokenPort, catalogQueryUseCase)
        every { embeddingPort.modelTag } returns "gemini-embedding-2@768"
        every { embeddingPort.embed(any()) } returns vector
        every { fcmTokenPort.existsByToken("fcm-1") } returns true
        every { fcmTokenPort.existsByToken("unknown") } returns false
        every { catalogQueryUseCase.getTopicCatalog() } returns TopicFixture.CATALOG
        every { persistencePort.countSince(windowStart) } returns 10
        every { persistencePort.save(any()) } answers {
            firstArg<NoticeQuestion>().withId(77)
        }
    }

    Given("등록되지 않은 기기가 질문하면") {
        reset()
        Then("임베딩하지 않고 기기 없음으로 거절한다") {
            shouldThrow<FcmTokenException> {
                service.prepare(PrepareQuestionCommand(QuestionAsker.App("unknown"), "질문"))
            }
            verify(exactly = 0) { embeddingPort.embed(any()) }
        }
    }

    Given("기기 하루 한도를 다 쓰면") {
        reset()
        every { persistencePort.countByAskerSince(NoticeQuestionAsker.APP, device, windowStart, NoticeQuestionStatus.NOT_FOUND) } returns 3

        Then("임베딩하지 않고 한도 초과로 거절한다 (근거 없던 질문은 세지 않는다)") {
            shouldThrow<QuestionException> {
                service.prepare(PrepareQuestionCommand(app, "질문"))
            }.baseErrorCode shouldBe QuestionErrorCode.DAILY_LIMIT_EXCEEDED
            verify(exactly = 0) { embeddingPort.embed(any()) }
        }
    }

    Given("서버 전체 하루 한도를 다 쓰면") {
        reset()
        every { persistencePort.countSince(windowStart) } returns 300

        Then("관리자도 서버 한도 초과로 거절한다") {
            shouldThrow<QuestionException> {
                service.prepare(PrepareQuestionCommand(QuestionAsker.Admin("1"), "질문"))
            }.baseErrorCode shouldBe QuestionErrorCode.SERVICE_DAILY_LIMIT_EXCEEDED
        }
    }

    Given("앱이 근거로 쓸 만한 공지가 있는 질문을 하면") {
        reset()
        every { persistencePort.countByAskerSince(NoticeQuestionAsker.APP, device, windowStart, NoticeQuestionStatus.NOT_FOUND) } returns 1
        val ranking = slot<NoticeSearchRanking>()
        every {
            persistencePort.searchChunks(vector, "gemini-embedding-2@768", capture(ranking), LocalDate.of(2026, 11, 20), 150)
        } returns listOf(hit(1, 1, 0.75), hit(2, 0, 0.70), hit(1, 0, 0.69), hit(3, 0, 0.65))
        val saved = slot<NoticeQuestion>()
        every { persistencePort.save(capture(saved)) } answers { saved.captured.withId(77) }

        val result = service.prepare(PrepareQuestionCommand(app, "동계 계절학기 수강신청 언제야?"))

        Then("근거 공지 · 프롬프트 · 서명을 주고 PREPARED 로 남긴다 (답변은 만들지 않는다)") {
            ranking.captured shouldBe NoticeSearchRanking(decayPerYear = 0.04, titlePenalty = 0.0)
            result.status shouldBe NoticeQuestionStatus.PREPARED
            result.sources.map { it.nttId } shouldContainExactly listOf(1001L, 1002L)
            result.messages.shouldNotBeNull().map { it.role } shouldContainExactly listOf("system", "user")
            result.finishExpiresAt shouldBe now.plus(Duration.ofMinutes(10))
            signer.verifyFinishToken(result.finishToken.shouldNotBeNull(), 77, device, now) shouldBe true
            result.quota.used shouldBe 2L
            result.quota.limit shouldBe 3
            result.quota.resetsAt shouldBe LocalDateTime.of(2026, 11, 21, 0, 0)
        }

        Then("기록에는 기기 해시만 남는다 (fcmToken 원문 없음)") {
            saved.captured.askedBy shouldBe NoticeQuestionAsker.APP
            saved.captured.askerKey shouldBe device
            saved.captured.status shouldBe NoticeQuestionStatus.PREPARED
            saved.captured.sourceNttIds shouldContainExactly listOf(1001L, 1002L)
            saved.captured.answer.shouldBeNull()
        }
    }

    Given("가장 비슷한 공지도 기준보다 낮으면") {
        reset()
        every { persistencePort.countByAskerSince(any(), any(), any(), any()) } returns 1
        every { persistencePort.searchChunks(any(), any(), any(), any(), any()) } returns listOf(hit(1, 0, 0.55))

        val result = service.prepare(PrepareQuestionCommand(app, "학교 와이파이 비밀번호"))

        Then("프롬프트 · 서명 없이 NOT_FOUND, 기기 횟수는 늘지 않는다") {
            result.status shouldBe NoticeQuestionStatus.NOT_FOUND
            result.messages.shouldBeNull()
            result.finishToken.shouldBeNull()
            result.sources.size shouldBe 1
            result.quota.used shouldBe 1L
        }
    }

    Given("관리자가 검색 설정을 바꿔 질문하면") {
        reset()
        every { persistencePort.countByAskedBySince(NoticeQuestionAsker.ADMIN, windowStart) } returns 4
        val ranking = slot<NoticeSearchRanking>()
        every { persistencePort.searchChunks(any(), any(), capture(ranking), any(), any()) } returns listOf(hit(1, 0, 0.8))

        val result = service.prepare(PrepareQuestionCommand(QuestionAsker.Admin("10"), "질문", decayPerYear = 0.5, titlePenalty = 0.02))

        Then("감점은 상한으로 줄이고, 관리자 전체 한도로 센다") {
            ranking.captured shouldBe NoticeSearchRanking(decayPerYear = 0.2, titlePenalty = 0.02)
            result.quota.used shouldBe 5L
            result.quota.limit shouldBe 50
        }
    }

    Given("앱이 받은 답변을 저장하면") {
        reset()
        val question = record()
        every { persistencePort.getQuestion(77) } returns question
        every { persistencePort.findNotices(any()) } answers {
            firstArg<Collection<Long>>().map { nttId ->
                Notice.crawled(
                    nttId = nttId, topic = TopicFixture.GENERAL_NEWS, title = "공지 $nttId", department = "학사팀",
                    contentUrl = "https://www.ut.ac.kr/$nttId", contentImageUrl = null,
                    registrationDate = LocalDate.of(2026, 11, 10), isAttachment = false,
                )
            }
        }
        val token = signer.finishToken(77, device, now.plusSeconds(600))

        val result = service.finish(
            FinishQuestionCommand(app, 77, " 마감은 11월 23일이에요 [2] [9] ", NoticeAnswerProvider.FACTCHAT, "gemini-3.5-flash-lite", token, 1612, 49)
        )

        Then("없는 근거 번호를 지우고 저장하고, 쓴 근거 공지를 돌려준다") {
            result.answer shouldBe "마감은 11월 23일이에요 [2]"
            result.citedNumbers shouldContainExactly listOf(2)
            result.sources.map { it.n to it.nttId } shouldContainExactly listOf(2 to 1002L)
            result.sources.first().boardName shouldBe "일반소식"
            question.status shouldBe NoticeQuestionStatus.ANSWERED
            question.answerProvider shouldBe NoticeAnswerProvider.FACTCHAT
            question.inputTokens shouldBe 1612
        }

        Then("같은 질문에 다시 저장하면 거절한다") {
            shouldThrow<QuestionException> {
                service.finish(FinishQuestionCommand(app, 77, "다시", NoticeAnswerProvider.GEMINI, "m", token))
            }.baseErrorCode shouldBe QuestionErrorCode.ALREADY_FINISHED
        }
    }

    Given("답변 저장 요청이 잘못되면") {
        reset()
        every { persistencePort.getQuestion(77) } answers { record() }
        val token = signer.finishToken(77, device, now.plusSeconds(600))

        Then("다른 기기의 질문은 없는 질문으로 다룬다") {
            every { fcmTokenPort.existsByToken("fcm-2") } returns true
            shouldThrow<QuestionException> {
                service.finish(FinishQuestionCommand(QuestionAsker.App("fcm-2"), 77, "답 [1]", NoticeAnswerProvider.GEMINI, "m", token))
            }.baseErrorCode shouldBe QuestionErrorCode.QUESTION_NOT_FOUND
        }

        Then("서명이 만료되었으면 거절한다") {
            val expired = signer.finishToken(77, device, now.minusSeconds(1))
            shouldThrow<QuestionException> {
                service.finish(FinishQuestionCommand(app, 77, "답 [1]", NoticeAnswerProvider.GEMINI, "m", expired))
            }.baseErrorCode shouldBe QuestionErrorCode.FINISH_TOKEN_INVALID
        }

        Then("근거 번호만 있는 빈 답변은 거절한다") {
            shouldThrow<QuestionException> {
                service.finish(FinishQuestionCommand(app, 77, " [9] ", NoticeAnswerProvider.GEMINI, "m", token))
            }.baseErrorCode shouldBe QuestionErrorCode.ANSWER_EMPTY
        }
    }

    Given("판정을 남기면") {
        reset()
        val question = record(askedBy = NoticeQuestionAsker.ADMIN, askerKey = "10", status = NoticeQuestionStatus.NOT_FOUND)
        every { persistencePort.getQuestion(77) } returns question

        Then("자기 질문에만 판정과 메모가 들어간다") {
            service.giveFeedback(QuestionAsker.Admin("10"), 77, NoticeQuestionVerdict.GOOD, "맞음") shouldBe true
            question.verdict shouldBe NoticeQuestionVerdict.GOOD
            shouldThrow<QuestionException> {
                service.giveFeedback(QuestionAsker.Admin("11"), 77, NoticeQuestionVerdict.BAD, null)
            }.baseErrorCode shouldBe QuestionErrorCode.QUESTION_NOT_FOUND
        }
    }

    Given("사용량을 물으면") {
        reset()
        every { persistencePort.countByAskerSince(NoticeQuestionAsker.APP, device, windowStart, NoticeQuestionStatus.NOT_FOUND) } returns 2

        Then("이 기기의 오늘(한국시간 0시부터) 질문 수와 한도를 준다") {
            val quota = service.getQuota(app)
            quota.used shouldBe 2L
            quota.limit shouldBe 3
            quota.resetsAt shouldBe LocalDateTime.of(2026, 11, 21, 0, 0)
        }
    }

})
