package com.fx.api.adapter.`in`.web.dto.topic

import com.fx.global.domain.CrawlableType
import com.fx.global.domain.MajorType
import com.fx.global.domain.MealType
import com.fx.global.domain.NoticeType
import com.fx.global.exception.TopicException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.context.MessageSource

class TypeResponseTest : BehaviorSpec({

    // CrawlableType 은 각 enum 의 init 블록에서 스스로를 등록하므로,
    // fromString/fromCode 를 쓰기 전에 enum 클래스를 먼저 로드해서 등록을 강제한다.
    NoticeType.entries
    MajorType.entries
    MealType.entries

    val messageSource = mockk<MessageSource>()
    every { messageSource.getMessage(any(), any(), any<String>(), any()) } answers { thirdArg() }

    Given("topic 이름으로 단건 조회") {
        When("존재하는 NoticeType 이름이면") {
            val resolved = CrawlableType.fromString("GENERAL_NEWS")
            val response = TypeResponse.from(resolved, messageSource)

            Then("topic, topicId, name 이 NoticeType 의 값과 일치한다") {
                response.topic shouldBe "GENERAL_NEWS"
                response.topicId shouldBe NoticeType.GENERAL_NEWS.code
                response.name shouldBe NoticeType.GENERAL_NEWS.category
                response.college shouldBe null
            }
        }

        When("존재하지 않는 topic 이름이면") {
            Then("TopicException 발생") {
                shouldThrow<TopicException> { CrawlableType.fromString("NOT_EXIST_TOPIC") }
            }
        }
    }

    Given("topicId 로 단건 조회") {
        When("존재하는 NoticeType code 이면") {
            val resolved = CrawlableType.fromCode(NoticeType.GENERAL_NEWS.code)
            val response = TypeResponse.from(resolved, messageSource)

            Then("topic 문자열 이름으로 정상 변환된다") {
                response.topic shouldBe "GENERAL_NEWS"
                response.topicId shouldBe NoticeType.GENERAL_NEWS.code
            }
        }

        When("존재하는 MajorType code 이면") {
            val resolved = CrawlableType.fromCode(MajorType.COMPUTER_ENGINEERING.code)
            val response = TypeResponse.from(resolved, messageSource)

            Then("college 필드까지 채워진다") {
                response.topic shouldBe "COMPUTER_ENGINEERING"
                response.topicId shouldBe MajorType.COMPUTER_ENGINEERING.code
                response.college shouldBe MajorType.COMPUTER_ENGINEERING.college
            }
        }

        When("존재하지 않는 topicId 이면") {
            Then("TopicException 발생") {
                shouldThrow<TopicException> { CrawlableType.fromCode(9999) }
            }
        }
    }

    Given("type(카테고리) 기준 전체 목록 조회 - 하위 호환") {
        When("NoticeType 전체 조회") {
            val responses = TypeResponse.fromNoticeTypes()

            Then("모든 항목에 topicId 가 채워진 채로 반환된다") {
                responses shouldHaveSize NoticeType.entries.size
                responses.forEach { response ->
                    val expected = NoticeType.valueOf(response.topic)
                    response.topicId shouldBe expected.code
                }
            }
        }

        When("MealType 전체 조회") {
            val responses = TypeResponse.fromMealTypes()

            Then("모든 항목에 topicId 가 채워진 채로 반환된다") {
                responses shouldHaveSize MealType.entries.size
                responses.forEach { response ->
                    val expected = MealType.valueOf(response.topic)
                    response.topicId shouldBe expected.code
                }
            }
        }

        When("MajorType 전체 조회") {
            val responses = TypeResponse.fromMajorTypes(messageSource)

            Then("모든 항목에 topicId, college 가 채워진 채로 반환된다") {
                responses shouldHaveSize MajorType.entries.size
                responses.forEach { response ->
                    val expected = MajorType.valueOf(response.topic)
                    response.topicId shouldBe expected.code
                    response.college shouldBe expected.college
                }
            }
        }
    }
})
