package com.fx.api.adapter.`in`.web.batch.dto

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import jakarta.validation.Validation

class BatchScheduleRequestTest : BehaviorSpec({

    val validator = Validation.buildDefaultValidatorFactory().validator

    fun createRequest(
        scheduleKey: String = "notice-crawl-notice-night",
        jobParameters: Map<String, String>? = mapOf("topicType" to "NOTICE"),
        enabled: Boolean? = true,
        description: String = "공지 게시판 야간 크롤링",
    ) =
        BatchScheduleCreateRequest(scheduleKey, "noticeCrawlJob", jobParameters, "0 0 23 * * *", enabled, description)

    fun invalidFields(request: Any): Set<String> =
        validator.validate(request)
            .map {
                it.propertyPath.toString()
            }
            .toSet()

    Given("스케줄 추가 요청") {

        When("올바른 값이면") {
            Then("통과한다") {
                invalidFields(createRequest()).shouldBeEmpty()
                invalidFields(createRequest(jobParameters = emptyMap())).shouldBeEmpty()
            }
        }

        When("스케줄 키에 영문 소문자 · 숫자 · 하이픈 외의 문자가 있거나 너무 길면") {
            Then("거절한다") {
                listOf("Notice-Crawl", "notice_crawl", "공지", "notice crawl", "a".repeat(101)).forEach {
                    invalidFields(createRequest(scheduleKey = it)) shouldBe setOf("scheduleKey")
                }
            }
        }

        When("자동 실행 여부 · 파라미터를 빠뜨리거나 설명이 비어 있으면") {
            Then("거절한다") {
                invalidFields(createRequest(enabled = null)) shouldBe setOf("enabled")
                invalidFields(createRequest(jobParameters = null)) shouldBe setOf("jobParameters")
                invalidFields(createRequest(description = " ")) shouldBe setOf("description")
                invalidFields(createRequest(description = "가".repeat(201))) shouldBe setOf("description")
            }
        }
    }

    Given("스케줄 수정 요청") {

        When("보내지 않은 값은") {
            Then("검증하지 않는다") {
                invalidFields(BatchScheduleUpdateRequest()).shouldBeEmpty()
            }
        }

        When("설명을 비우거나 너무 길게 보내면") {
            Then("거절한다") {
                invalidFields(BatchScheduleUpdateRequest(description = " ")) shouldBe setOf("description")
                invalidFields(BatchScheduleUpdateRequest(description = "가".repeat(201))) shouldBe setOf("description")
            }
        }
    }

    Given("자동 실행 켜기 · 끄기 요청") {

        When("enabled 를 빠뜨리면") {
            Then("거절한다") {
                invalidFields(BatchScheduleEnabledRequest(null)) shouldBe setOf("enabled")
            }
        }
    }

})
