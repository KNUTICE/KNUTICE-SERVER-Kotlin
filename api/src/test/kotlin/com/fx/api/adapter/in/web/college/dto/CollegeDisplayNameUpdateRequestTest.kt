package com.fx.api.adapter.`in`.web.college.dto

import com.fx.api.adapter.`in`.web.college.dto.CollegeDisplayNameUpdateRequest.DisplayNameRequest
import com.fx.common.domain.i18n.LocalizedText
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import jakarta.validation.Validation

class CollegeDisplayNameUpdateRequestTest : BehaviorSpec({

    val validator = Validation.buildDefaultValidatorFactory().validator

    fun request(ko: String? = "공과대학", en: String? = "College of Engineering", ja: String? = "工学部") =
        CollegeDisplayNameUpdateRequest(DisplayNameRequest(ko, en, ja))

    fun invalidFields(request: Any): Set<String> =
        validator.validate(request)
            .map {
                it.propertyPath.toString()
            }
            .toSet()

    Given("단과대 표시명 수정 요청") {

        When("올바른 값이면") {
            Then("통과한다") {
                invalidFields(request()).shouldBeEmpty()
                invalidFields(request(en = null, ja = null)).shouldBeEmpty()
                invalidFields(request(ko = "가".repeat(100))).shouldBeEmpty()
            }
        }

        When("표시명을 빠뜨리거나 한국어 표시명이 비어 있으면") {
            Then("거절한다") {
                invalidFields(CollegeDisplayNameUpdateRequest(null)) shouldBe setOf("displayName")
                invalidFields(request(ko = null)) shouldBe setOf("displayName.ko")
                invalidFields(request(ko = " ")) shouldBe setOf("displayName.ko")
            }
        }

        When("표시명이 100자를 넘으면") {
            Then("거절한다") {
                invalidFields(request(ko = "가".repeat(101))) shouldBe setOf("displayName.ko")
                invalidFields(request(en = "a".repeat(101))) shouldBe setOf("displayName.en")
                invalidFields(request(ja = "가".repeat(101))) shouldBe setOf("displayName.ja")
            }
        }

        When("영어 · 일본어가 null 이거나 빈 문자열이면") {
            Then("비운다") {
                request(en = null, ja = "").toCommand("ENGINEERING").displayName shouldBe LocalizedText("공과대학")
                request(en = " ", ja = null).toCommand("ENGINEERING").displayName shouldBe LocalizedText("공과대학")
            }
        }

        When("명령으로 바꾸면") {
            Then("경로의 단과대 키와 언어별 표시명을 담는다") {
                val command = request().toCommand("ENGINEERING")

                command.collegeKey shouldBe "ENGINEERING"
                command.displayName shouldBe LocalizedText("공과대학", "College of Engineering", "工学部")
            }
        }
    }

})
