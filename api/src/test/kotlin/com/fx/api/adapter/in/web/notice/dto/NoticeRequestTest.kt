package com.fx.api.adapter.`in`.web.notice.dto

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import jakarta.validation.Validation
import java.time.LocalDate

class NoticeRequestTest : BehaviorSpec({

    val validator = Validation.buildDefaultValidatorFactory().validator

    fun request(nttId: Long = 1L, contentImageUrl: String? = null) =
        NoticeRequest(
            nttId = nttId,
            title = "관리자 공지",
            contentUrl = "https://www.ut.ac.kr/notice/1",
            contentImageUrl = contentImageUrl,
            department = "학사팀",
            registrationDate = LocalDate.of(2026, 10, 12),
            isAttachment = false,
            topic = "GENERAL_NEWS",
        )

    fun invalidFields(request: NoticeRequest): Set<String> =
        validator.validate(request)
            .map {
                it.propertyPath.toString()
            }
            .toSet()

    Given("공지 등록 · 수정 요청") {

        When("올바른 값이면") {
            Then("통과한다") {
                invalidFields(request()).shouldBeEmpty()
                invalidFields(request(contentImageUrl = "https://www.ut.ac.kr/images/1.png")).shouldBeEmpty()
            }
        }

        When("게시글 번호가 0 이하면") {
            Then("거절한다") {
                listOf(0L, -5L).forEach {
                    invalidFields(request(nttId = it)) shouldBe setOf("nttId")
                }
            }
        }

        When("이미지 URL 이 URL 형식이 아니면") {
            Then("거절한다") {
                listOf("not a url", "www.ut.ac.kr/1.png", " ").forEach {
                    invalidFields(request(contentImageUrl = it)) shouldBe setOf("contentImageUrl")
                }
            }
        }
    }

})
