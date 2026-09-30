package com.fx.api.application.service.topic

import com.fx.api.fixture.TopicFixture
import com.fx.common.application.port.`in`.CatalogQueryUseCase
import com.fx.common.domain.TopicType
import com.fx.common.exception.TopicException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class TopicResolverTest : BehaviorSpec({

    val catalogQueryUseCase = mockk<CatalogQueryUseCase>()
    every { catalogQueryUseCase.getTopicCatalog() } returns TopicFixture.CATALOG
    val topicResolver = TopicResolver(catalogQueryUseCase)

    Given("토픽 이름으로 찾기") {
        When("존재하는 이름이면") {
            Then("토픽을 반환한다") {
                topicResolver.byName("GENERAL_NEWS") shouldBe TopicFixture.GENERAL_NEWS
                topicResolver.byName("COMPUTER_SOFTWARE", TopicType.MAJOR) shouldBe TopicFixture.COMPUTER_SOFTWARE
            }
        }

        When("존재하지 않는 이름이면") {
            Then("TopicException 이 발생한다") {
                shouldThrow<TopicException> { topicResolver.byName("NOT_EXIST_TOPIC") }
            }
        }

        When("기대한 유형이 아니면") {
            Then("TopicException 이 발생한다") {
                shouldThrow<TopicException> { topicResolver.byName("GENERAL_NEWS", TopicType.MEAL) }
            }
        }
    }

    Given("토픽 코드로 찾기") {
        When("존재하는 코드면") {
            Then("토픽을 반환한다") {
                topicResolver.byCode(900) shouldBe TopicFixture.STUDENT_CAFETERIA
            }
        }

        When("존재하지 않는 코드면") {
            Then("TopicException 이 발생한다") {
                shouldThrow<TopicException> { topicResolver.byCode(9999) }
            }
        }
    }

    Given("이름 또는 코드로 찾기") {
        When("둘 다 있으면") {
            Then("코드를 우선한다") {
                topicResolver.byNameOrCode("GENERAL_NEWS", 300) shouldBe TopicFixture.COMPUTER_SOFTWARE
            }
        }

        When("둘 다 없으면") {
            Then("null 을 반환한다") {
                topicResolver.byNameOrCode(null, null) shouldBe null
            }
        }
    }

})
