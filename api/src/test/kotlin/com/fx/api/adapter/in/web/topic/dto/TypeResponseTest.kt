package com.fx.api.adapter.`in`.web.topic.dto

import com.fx.api.fixture.TopicFixture
import com.fx.common.domain.i18n.Language
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class TypeResponseTest : BehaviorSpec({

    Given("공지 토픽 변환") {
        When("한국어로 변환하면") {
            val response = TypeResponse.from(TopicFixture.GENERAL_NEWS, Language.KO)

            Then("topic 은 이름, topicId 는 코드, name 은 표시명이고 college 는 null 이다") {
                response.topic shouldBe "GENERAL_NEWS"
                response.topicId shouldBe 1
                response.name shouldBe "일반소식"
                response.college shouldBe null
            }
        }

        When("번역이 없는 언어로 변환하면") {
            val response = TypeResponse.from(TopicFixture.GENERAL_NEWS, Language.JA)

            Then("한국어 표시명을 쓴다") {
                response.name shouldBe "일반소식"
            }
        }
    }

    Given("학과 토픽 변환") {
        When("영어로 변환하면") {
            val response = TypeResponse.from(TopicFixture.COMPUTER_SOFTWARE, Language.EN)

            Then("표시명과 단과대 이름을 영어로 채운다") {
                response.topic shouldBe "COMPUTER_SOFTWARE"
                response.topicId shouldBe 300
                response.name shouldBe "Computer Software"
                response.college shouldBe "College of Engineering"
            }
        }

        When("일본어로 변환하면") {
            val response = TypeResponse.from(TopicFixture.COMPUTER_SOFTWARE, Language.JA)

            Then("학과 표시명은 한국어로, 단과대 이름은 일본어로 채운다") {
                response.name shouldBe "컴퓨터소프트웨어학과"
                response.college shouldBe "工学部"
            }
        }
    }

})
