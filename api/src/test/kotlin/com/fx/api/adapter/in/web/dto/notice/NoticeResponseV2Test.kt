package com.fx.api.adapter.`in`.web.dto.notice

import com.fx.global.domain.Notice
import com.fx.global.domain.NoticeType
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class NoticeResponseV2Test : BehaviorSpec({

    Given("Notice 도메인 변환") {
        val notice = Notice(
            nttId = 1123319L,
            title = "테스트 공지",
            department = "학생과",
            contentUrl = "https://www.ut.ac.kr/notice/1123319",
            registrationDate = LocalDate.of(2026, 7, 21),
            isAttachment = false,
            topic = NoticeType.GENERAL_NEWS
        )

        When("NoticeResponseV2 로 변환하면") {
            val response = NoticeResponseV2.from(notice)

            Then("topicId 는 topic 의 정수 code 를 반환한다") {
                response.topicId shouldBe NoticeType.GENERAL_NEWS.code
            }

            Then("나머지 필드도 도메인 값과 일치한다") {
                response.nttId shouldBe notice.nttId
                response.topic shouldBe NoticeType.GENERAL_NEWS
                response.isContentSummary shouldBe false
            }
        }
    }
})
