package com.fx.api.application.service.fcmtoken

import com.fx.api.application.port.out.fcmtoken.FcmTokenPersistencePort
import com.fx.api.fixture.TopicFixture
import com.fx.common.application.port.`in`.CatalogQueryUseCase
import com.fx.common.domain.DeviceType
import com.fx.common.domain.TopicType
import com.fx.common.domain.fcmtoken.FcmToken
import com.fx.common.domain.i18n.Language
import com.fx.common.exception.FcmTokenException
import com.fx.common.exception.errorcode.FcmTokenErrorCode
import com.fx.persistence.withId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class FcmTokenQueryServiceTest : BehaviorSpec({

    val fcmTokenPersistencePort = mockk<FcmTokenPersistencePort>()
    val catalogQueryUseCase = mockk<CatalogQueryUseCase>()
    every {
        catalogQueryUseCase.getTopicCatalog()
    } returns TopicFixture.CATALOG
    val fcmTokenQueryService = FcmTokenQueryService(fcmTokenPersistencePort, catalogQueryUseCase)

    Given("구독 토픽 조회") {
        val token = FcmToken("fcmToken", DeviceType.iOS).withId(1L)
        every {
            fcmTokenPersistencePort.getByToken(token.token)
        } returns token
        // 9999 는 삭제된 토픽이라 카탈로그에 없다
        every {
            fcmTokenPersistencePort.findSubscribedTopicCodes(1L)
        } returns setOf(900, 2, 300, 1, 9999)

        When("공지 토픽을 조회하면") {
            Then("공지 토픽만 code 오름차순으로 반환한다") {
                fcmTokenQueryService.getMyTopics(token.token, TopicType.NOTICE)
                    .map {
                        it.name
                    } shouldContainExactly listOf("GENERAL_NEWS", "SCHOLARSHIP_NEWS")
            }
        }

        When("학과 토픽을 조회하면") {
            Then("학과 토픽만 반환한다") {
                fcmTokenQueryService.getMyTopics(token.token, TopicType.MAJOR)
                    .map {
                        it.name
                    } shouldContainExactly listOf("COMPUTER_SOFTWARE")
            }
        }

        When("학식 토픽을 조회하면") {
            Then("학식 토픽만 반환하고 카탈로그에 없는 구독은 뺀다") {
                fcmTokenQueryService.getMyTopics(token.token, TopicType.MEAL)
                    .map {
                        it.name
                    } shouldContainExactly listOf("STUDENT_CAFETERIA")
            }
        }
    }

    Given("토큰이 존재하지 않는 경우") {
        every {
            fcmTokenPersistencePort.getByToken("unknown")
        } throws FcmTokenException(FcmTokenErrorCode.TOKEN_NOT_FOUND)

        When("토픽을 조회하면") {
            Then("FcmTokenException 이 발생한다") {
                shouldThrow<FcmTokenException> {
                    fcmTokenQueryService.getMyTopics("unknown", TopicType.NOTICE)
                }
            }
        }
    }

    Given("알림 언어 조회") {
        When("언어를 바꾼 적이 없는 토큰이면") {
            every {
                fcmTokenPersistencePort.getByToken("default-token")
            } returns FcmToken("default-token", DeviceType.iOS)

            Then("한국어를 반환한다") {
                fcmTokenQueryService.getLanguage("default-token") shouldBe Language.KO
            }
        }

        When("지역 코드가 붙은 언어가 저장돼 있으면") {
            every {
                fcmTokenPersistencePort.getByToken("ja-token")
            } returns FcmToken("ja-token", DeviceType.AOS, "ja-JP")

            Then("발송할 때와 같은 언어로 해석해 반환한다") {
                fcmTokenQueryService.getLanguage("ja-token") shouldBe Language.JA
            }
        }

        When("저장되지 않은 토큰이면") {
            every {
                fcmTokenPersistencePort.getByToken("unknown")
            } throws FcmTokenException(FcmTokenErrorCode.TOKEN_NOT_FOUND)

            Then("FcmTokenException 이 발생한다") {
                shouldThrow<FcmTokenException> {
                    fcmTokenQueryService.getLanguage("unknown")
                }
            }
        }
    }

})
