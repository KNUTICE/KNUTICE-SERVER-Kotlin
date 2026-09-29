package com.fx.api.adapter.`in`.web.dto.topic

import com.fx.api.fixture.TopicFixture
import com.fx.common.domain.i18n.Language
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class TopicResponseV2Test : BehaviorSpec({

    Given("구독 토픽 목록 변환") {
        When("공지와 학과 토픽이 섞여 있으면") {
            val response = TopicResponseV2.from(
                listOf(TopicFixture.COMPUTER_SOFTWARE, TopicFixture.GENERAL_NEWS),
                Language.KO,
            )

            Then("topicId 오름차순의 토픽 객체 목록으로 반환한다") {
                response.subscribedTopics.map { it.topicId } shouldContainExactly listOf(1, 300)
            }

            Then("각 객체는 topic · topicId · name · college 를 채운다") {
                response.subscribedTopics.first { it.topic == "GENERAL_NEWS" }.college shouldBe null
                response.subscribedTopics.first { it.topic == "COMPUTER_SOFTWARE" }.college shouldBe "공과대학"
            }
        }

        When("구독한 토픽이 없으면") {
            val response = TopicResponseV2.from(emptyList(), Language.KO)

            Then("빈 목록을 반환한다") {
                response.subscribedTopics shouldBe emptyList()
            }
        }
    }

    Given("v1 구독 토픽 변환") {
        When("토픽 목록을 변환하면") {
            val response = TopicResponse.from(listOf(TopicFixture.GENERAL_NEWS, TopicFixture.SCHOLARSHIP_NEWS))

            Then("토픽 이름 집합을 순서대로 반환한다") {
                response.subscribedTopics.toList() shouldContainExactly listOf("GENERAL_NEWS", "SCHOLARSHIP_NEWS")
            }
        }
    }

})
