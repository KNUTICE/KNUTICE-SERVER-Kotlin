package com.fx.api.adapter.`in`.web.batch.dto

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import jakarta.validation.Validation

class BatchRunRequestDtoTest : BehaviorSpec({

    val validator = Validation.buildDefaultValidatorFactory().validator

    fun invalidFields(request: Any): Set<String> =
        validator.validate(request)
            .map {
                it.propertyPath.toString()
            }
            .toSet()

    Given("수동 실행 요청") {

        When("파라미터가 없는 Job 에 빈 객체를 보내면") {
            Then("통과한다") {
                invalidFields(BatchRunRequestCreateRequest("silentPushJob", emptyMap())).shouldBeEmpty()
            }
        }

        When("Job 이름이 비었거나 파라미터를 빠뜨리면") {
            Then("거절한다") {
                invalidFields(BatchRunRequestCreateRequest(" ", emptyMap())) shouldBe setOf("jobName")
                invalidFields(BatchRunRequestCreateRequest("silentPushJob", null)) shouldBe setOf("jobParameters")
            }
        }
    }

    Given("수동 실행 요청 목록 조건") {

        When("size 를 보내지 않으면") {
            Then("20 개씩 읽는다") {
                BatchRunRequestSearchParam().toCommand().size shouldBe 20
            }
        }

        When("size 가 1~100 을 벗어나면") {
            Then("거절한다") {
                listOf(0, 101).forEach {
                    invalidFields(BatchRunRequestSearchParam(size = it)) shouldBe setOf("size")
                }
                invalidFields(BatchRunRequestSearchParam(size = 100)).shouldBeEmpty()
            }
        }
    }

})
