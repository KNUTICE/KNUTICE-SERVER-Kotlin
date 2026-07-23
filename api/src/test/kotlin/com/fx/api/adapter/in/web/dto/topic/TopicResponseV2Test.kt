package com.fx.api.adapter.`in`.web.dto.topic

import com.fx.global.domain.MajorType
import com.fx.global.domain.MealType
import com.fx.global.domain.NoticeType
import com.fx.global.exception.TopicException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeSortedBy
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.context.MessageSource

class TopicResponseV2Test : BehaviorSpec({

    // CrawlableType 은 각 enum 의 init 블록에서 스스로를 등록하므로,
    // fromString/fromCode 를 쓰기 전에 enum 클래스를 먼저 로드해서 등록을 강제한다.
    NoticeType.entries
    MajorType.entries
    MealType.entries

    val messageSource = mockk<MessageSource>()
    every { messageSource.getMessage(any(), any(), any<String>(), any()) } answers { thirdArg() }

    Given("구독 토픽 문자열 집합 변환") {
        When("NoticeType 과 MajorType 문자열이 섞여 있으면") {
            val response = TopicResponseV2.from(
                setOf("COMPUTER_ENGINEERING", "GENERAL_NEWS"),
                messageSource
            )

            Then("subscribedTopics 는 topicId 오름차순의 신규 객체 리스트로 반환된다") {
                response.subscribedTopics shouldHaveSize 2
                response.subscribedTopics.shouldBeSortedBy { it.topicId }
            }

            Then("각 객체는 topic·topicId·name·college 를 채워서 반환한다") {
                val general = response.subscribedTopics.first { it.topic == "GENERAL_NEWS" }
                general.topicId shouldBe NoticeType.GENERAL_NEWS.code
                general.name shouldBe NoticeType.GENERAL_NEWS.category
                general.college shouldBe null

                val major = response.subscribedTopics.first { it.topic == "COMPUTER_ENGINEERING" }
                major.topicId shouldBe MajorType.COMPUTER_ENGINEERING.code
                major.college shouldBe MajorType.COMPUTER_ENGINEERING.college
            }
        }

        When("구독한 토픽이 없으면") {
            val response = TopicResponseV2.from(emptySet(), messageSource)

            Then("빈 집합과 빈 리스트를 반환한다") {
                response.subscribedTopics shouldBe emptyList()
            }
        }

        When("서버에 존재하지 않는 토픽 문자열이 저장되어 있으면") {
            Then("TopicException 발생") {
                shouldThrow<TopicException> {
                    TopicResponseV2.from(setOf("NOT_EXIST_TOPIC"), messageSource)
                }
            }
        }
    }
})
