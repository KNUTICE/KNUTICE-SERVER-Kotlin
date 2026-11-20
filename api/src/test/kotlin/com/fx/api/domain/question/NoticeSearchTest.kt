package com.fx.api.domain.question

import com.fx.common.domain.notice.NoticeChunkType
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.time.LocalDate
import java.time.LocalDateTime

class NoticeSearchTest : BehaviorSpec({

    fun hit(noticeId: Long, chunkIndex: Int, score: Double, content: String = "[학사공지사항] 공지 $noticeId\n본문 $noticeId-$chunkIndex") =
        NoticeChunkHit(
            noticeId = noticeId,
            nttId = noticeId * 100,
            title = "공지 $noticeId",
            registrationDate = LocalDate.of(2026, 11, 10),
            contentUrl = "https://www.ut.ac.kr/$noticeId",
            chunkIndex = chunkIndex,
            chunkType = NoticeChunkType.BODY,
            content = content,
            similarity = score + 0.01,
            score = score,
        )

    Given("점수 순 청크를 공지 단위로 묶을 때") {
        val hits = listOf(
            hit(noticeId = 1, chunkIndex = 2, score = 0.80),
            hit(noticeId = 2, chunkIndex = 0, score = 0.79),
            hit(noticeId = 1, chunkIndex = 0, score = 0.78),
            hit(noticeId = 3, chunkIndex = 0, score = 0.77),
            hit(noticeId = 1, chunkIndex = 1, score = 0.76),
            hit(noticeId = 1, chunkIndex = 3, score = 0.75),
        )

        Then("처음 나온 공지 순서대로 번호를 매기고 상위 공지 수까지만 고른다") {
            NoticeSourcePicker.pick(hits, topNotices = 2, chunksPerNotice = 3).map {
                it.n to it.noticeId
            } shouldContainExactly listOf(1 to 1L, 2 to 2L)
        }

        Then("공지마다 청크는 정해진 수까지, 청크 순서대로 넘긴다") {
            val first = NoticeSourcePicker.pick(hits, topNotices = 3, chunksPerNotice = 3).first()
            first.hits.map {
                it.chunkIndex
            } shouldContainExactly listOf(0, 1, 2)
            first.score shouldBe 0.80
        }
    }

    Given("청크 내용") {
        Then("첫 줄의 게시판 이름과 첫 줄을 뗀 본문을 읽는다") {
            val chunk = hit(1, 0, 0.7, "[장학안내] 창업 마일리지 신청 안내\n제출기한 12월 31일")
            chunk.boardName shouldBe "장학안내"
            chunk.body shouldBe "제출기한 12월 31일"
        }

        Then("제목만 있는 청크는 본문이 비어 있다") {
            hit(1, 0, 0.7, "[행사안내] 2024년 의왕시 일자리박람회").body shouldBe ""
        }
    }

    Given("답변 모델에 넘기는 글") {
        Then("근거 번호 · 제목 · 게시일과 첫 줄을 뗀 본문, 지금 시각(한국시간), 질문을 담는다") {
            val sources = NoticeSourcePicker.pick(
                listOf(hit(1, 0, 0.8, "[학사공지사항] 계절학기 수강신청\n1차 11. 17. ~ 11. 20.")),
                topNotices = 5,
                chunksPerNotice = 3,
            )
            val prompt = NoticeAnswerPrompt.user("수강신청 언제야?", sources, LocalDateTime.of(2026, 11, 20, 10, 5))

            prompt shouldContain "[지금] 2026-11-20 (금) 10:05 (한국시간)"
            prompt shouldContain "[1] 제목: 공지 1 (게시판 학사공지사항, 게시일 2026-11-10)"
            prompt shouldContain "1차 11. 17. ~ 11. 20."
            prompt shouldNotContain "[학사공지사항] 계절학기 수강신청"
            prompt shouldContain "[질문]\n수강신청 언제야?"
        }
    }

    Given("하루 한도 구간 (한국시간 17시 초기화)") {
        Then("17시 전이면 어제 17시부터, 17시부터는 오늘 17시부터 센다") {
            QuestionWindow.start(LocalDateTime.of(2026, 11, 19, 16, 59), 17) shouldBe LocalDateTime.of(2026, 11, 18, 17, 0)
            QuestionWindow.start(LocalDateTime.of(2026, 11, 19, 17, 0), 17) shouldBe LocalDateTime.of(2026, 11, 19, 17, 0)
            QuestionWindow.nextReset(LocalDateTime.of(2026, 11, 19, 10, 0), 17) shouldBe LocalDateTime.of(2026, 11, 19, 17, 0)
        }
    }

    Given("관리자 판정") {
        Then("메모는 앞뒤 공백을 지우고 비어 있으면 남기지 않는다") {
            val question = NoticeQuestion(
                askedBy = NoticeQuestionAsker.ADMIN,
                askerKey = "1",
                question = "질문",
                status = NoticeQuestionStatus.ANSWERED,
                answer = "답",
                sourceNttIds = listOf(3, 1),
                topSimilarity = 0.7,
                decayPerYear = 0.04,
                titlePenalty = 0.0,
                answerModel = "m",
                inputTokens = 1,
                outputTokens = 1,
                elapsedMs = 10,
            )
            question.sourceNttIds shouldContainExactly listOf(3L, 1L)

            question.giveFeedback(NoticeQuestionVerdict.BAD, "  ")
            question.verdict shouldBe NoticeQuestionVerdict.BAD
            question.memo shouldBe null

            question.giveFeedback(NoticeQuestionVerdict.GOOD, " 날짜 맞음 ")
            question.memo shouldBe "날짜 맞음"
        }
    }

})
