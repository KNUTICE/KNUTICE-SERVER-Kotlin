package com.fx.api.adapter.`in`.web.dto.notice

import com.fx.api.fixture.TopicFixture
import com.fx.common.domain.notice.Notice
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class NoticeResponseV2Test : BehaviorSpec({

    Given("공지 변환") {
        val notice = Notice.crawled(
            nttId = 1123319L,
            topic = TopicFixture.COMPUTER_SOFTWARE,
            title = "테스트 공지",
            department = "학생과",
            contentUrl = "https://www.ut.ac.kr/notice/1123319",
            contentImageUrl = null,
            registrationDate = LocalDate.of(2026, 7, 21),
            isAttachment = false,
        )

        When("NoticeResponseV2 로 변환하면") {
            val response = NoticeResponseV2.from(notice)

            Then("topic 은 토픽 이름, topicId 는 토픽 코드를 반환한다") {
                response.topic shouldBe "COMPUTER_SOFTWARE"
                response.topicId shouldBe 300
            }

            Then("요약 전이므로 isContentSummary 는 false 다") {
                response.nttId shouldBe 1123319L
                response.isContentSummary shouldBe false
            }
        }

        When("관리자가 요약과 함께 등록한 공지를 변환하면") {
            val adminNotice = Notice.registeredByAdmin(
                nttId = 1L, topic = TopicFixture.GENERAL_NEWS, title = "관리자 공지", department = "학사팀",
                contentUrl = "https://www.ut.ac.kr/notice/1", contentImageUrl = null,
                registrationDate = LocalDate.of(2026, 7, 21), isAttachment = true, hasSummary = true,
            )

            Then("isContentSummary 는 true 다") {
                NoticeResponseV2.from(adminNotice).isContentSummary shouldBe true
                NoticeResponse.from(adminNotice).topic shouldBe "GENERAL_NEWS"
            }
        }
    }

})
