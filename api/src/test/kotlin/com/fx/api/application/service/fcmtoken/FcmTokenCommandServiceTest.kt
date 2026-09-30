package com.fx.api.application.service.fcmtoken

import com.fx.api.application.port.`in`.fcmtoken.dto.FcmTokenSaveCommand
import com.fx.api.application.port.`in`.fcmtoken.dto.FcmTokenUpdateCommand
import com.fx.api.application.port.`in`.fcmtoken.dto.TopicUpdateCommand
import com.fx.api.application.port.out.fcmtoken.FcmTokenPersistencePort
import com.fx.api.application.service.topic.TopicResolver
import com.fx.api.fixture.TopicFixture
import com.fx.common.application.port.`in`.CatalogQueryUseCase
import com.fx.common.domain.DeviceType
import com.fx.common.domain.TopicType
import com.fx.common.domain.fcmtoken.FcmToken
import com.fx.common.exception.FcmTokenException
import com.fx.common.exception.TopicException
import com.fx.common.exception.errorcode.FcmTokenErrorCode
import com.fx.persistence.withId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class FcmTokenCommandServiceTest : BehaviorSpec({

    val fcmTokenPersistencePort = mockk<FcmTokenPersistencePort>(relaxed = true)
    val catalogQueryUseCase = mockk<CatalogQueryUseCase>()
    every {
        catalogQueryUseCase.getTopicCatalog()
    } returns TopicFixture.CATALOG
    val fcmTokenCommandService = FcmTokenCommandService(
        fcmTokenPersistencePort, catalogQueryUseCase, TopicResolver(catalogQueryUseCase)
    )

    Given("새로운 토큰 저장") {
        val command = FcmTokenSaveCommand(fcmToken = "newFcmToken", deviceType = DeviceType.iOS)

        When("존재하지 않은 토큰인 경우") {
            clearMocks(fcmTokenPersistencePort)
            every {
                fcmTokenPersistencePort.findByToken(command.fcmToken)
            } returns null

            Then("앱에 노출된 공지 · 학식 토픽을 구독한 새 토큰이 저장된다") {
                fcmTokenCommandService.saveFcmToken(command) shouldBe true

                verify(exactly = 1) {
                    fcmTokenPersistencePort.create(
                        match {
                            it.token == command.fcmToken && it.isActive && it.deviceType == DeviceType.iOS
                        },
                        match { topics ->
                            topics.map {
                                it.name
                            }.toSet() == setOf("GENERAL_NEWS", "SCHOLARSHIP_NEWS", "STUDENT_CAFETERIA")
                        }
                    )
                }
            }
        }

        When("이미 토큰이 존재하는 경우") {
            clearMocks(fcmTokenPersistencePort)
            val existing = FcmToken(command.fcmToken, DeviceType.iOS).withId(1L).apply {
                deactivate()
            }
            every {
                fcmTokenPersistencePort.findByToken(command.fcmToken)
            } returns existing

            Then("기존 토큰을 다시 활성화하고 새로 만들지 않는다") {
                fcmTokenCommandService.saveFcmToken(command) shouldBe true
                existing.isActive shouldBe true
                verify(exactly = 0) {
                    fcmTokenPersistencePort.create(any(), any())
                }
            }
        }

        When("토큰이 비어 있는 경우") {
            Then("TOKEN_INVALID 예외가 발생한다") {
                val exception = shouldThrow<FcmTokenException> {
                    fcmTokenCommandService.saveFcmToken(FcmTokenSaveCommand(fcmToken = "   ", deviceType = DeviceType.AOS))
                }
                exception.baseErrorCode shouldBe FcmTokenErrorCode.TOKEN_INVALID
            }
        }
    }

    Given("앱이 새 토큰을 받아 토큰을 갱신하는 경우") {
        val command = FcmTokenUpdateCommand(oldFcmToken = "oldFcmToken", newFcmToken = "newFcmToken", deviceType = DeviceType.iOS)

        When("기존 토큰만 있는 경우") {
            clearMocks(fcmTokenPersistencePort)
            val oldToken = FcmToken(command.oldFcmToken, DeviceType.AOS).withId(1L)
            every {
                fcmTokenPersistencePort.findByToken(command.oldFcmToken)
            } returns oldToken
            every {
                fcmTokenPersistencePort.findByToken(command.newFcmToken)
            } returns null

            Then("같은 행의 토큰 값을 바꿔 id · 구독을 유지한다") {
                fcmTokenCommandService.updateFcmToken(command) shouldBe true
                oldToken.token shouldBe command.newFcmToken
                oldToken.isActive shouldBe true
                verify(exactly = 0) {
                    fcmTokenPersistencePort.create(any(), any())
                }
                verify(exactly = 0) {
                    fcmTokenPersistencePort.copySubscriptions(any(), any())
                }
            }
        }

        When("기존 토큰과 새 토큰이 모두 있는 경우") {
            clearMocks(fcmTokenPersistencePort)
            val oldToken = FcmToken(command.oldFcmToken, DeviceType.iOS).withId(1L)
            val newToken = FcmToken(command.newFcmToken, DeviceType.iOS).withId(2L).apply {
                deactivate()
            }
            every {
                fcmTokenPersistencePort.findByToken(command.oldFcmToken)
            } returns oldToken
            every {
                fcmTokenPersistencePort.findByToken(command.newFcmToken)
            } returns newToken

            Then("새 토큰의 구독을 기존 토큰과 같게 맞추고 기존 토큰은 비활성화한다") {
                fcmTokenCommandService.updateFcmToken(command) shouldBe true
                verify(exactly = 1) {
                    fcmTokenPersistencePort.copySubscriptions(1L, 2L)
                }
                newToken.isActive shouldBe true
                oldToken.isActive shouldBe false
            }
        }

        When("새 토큰만 있는 경우") {
            clearMocks(fcmTokenPersistencePort)
            val newToken = FcmToken(command.newFcmToken, DeviceType.iOS).withId(2L).apply {
                deactivate()
            }
            every {
                fcmTokenPersistencePort.findByToken(command.oldFcmToken)
            } returns null
            every {
                fcmTokenPersistencePort.findByToken(command.newFcmToken)
            } returns newToken

            Then("새 토큰을 활성화하고 구독은 그대로 둔다") {
                fcmTokenCommandService.updateFcmToken(command) shouldBe true
                newToken.isActive shouldBe true
                verify(exactly = 0) {
                    fcmTokenPersistencePort.create(any(), any())
                }
            }
        }

        When("두 토큰 모두 없는 경우") {
            clearMocks(fcmTokenPersistencePort)
            every {
                fcmTokenPersistencePort.findByToken(any())
            } returns null

            Then("기본 토픽을 구독한 새 토큰을 만든다") {
                fcmTokenCommandService.updateFcmToken(command) shouldBe true
                verify(exactly = 1) {
                    fcmTokenPersistencePort.create(match {
                        it.token == command.newFcmToken
                    }, any())
                }
            }
        }
    }

    Given("토픽 구독 변경") {
        val token = FcmToken("fcmToken", DeviceType.iOS).withId(1L)

        When("토큰이 존재하지 않는 경우") {
            clearMocks(fcmTokenPersistencePort)
            every {
                fcmTokenPersistencePort.getByToken("unknown")
            } throws FcmTokenException(FcmTokenErrorCode.TOKEN_NOT_FOUND)

            Then("FcmTokenException 이 발생한다") {
                shouldThrow<FcmTokenException> {
                    fcmTokenCommandService.updateTopic(
                        TopicUpdateCommand(fcmToken = "unknown", topicType = TopicType.NOTICE, topicName = "GENERAL_NEWS", enabled = true)
                    )
                }
            }
        }

        When("v1 토픽 이름으로 구독하는 경우") {
            clearMocks(fcmTokenPersistencePort)
            every {
                fcmTokenPersistencePort.getByToken(token.token)
            } returns token

            Then("해당 토픽을 구독한다") {
                fcmTokenCommandService.updateTopic(
                    TopicUpdateCommand(fcmToken = token.token, topicType = TopicType.NOTICE, topicName = "GENERAL_NEWS", enabled = true)
                ) shouldBe true
                verify(exactly = 1) {
                    fcmTokenPersistencePort.subscribe(1L, TopicFixture.GENERAL_NEWS)
                }
            }
        }

        When("v2 토픽 코드로 해제하는 경우") {
            clearMocks(fcmTokenPersistencePort)
            every {
                fcmTokenPersistencePort.getByToken(token.token)
            } returns token

            Then("해당 토픽 구독을 해제한다") {
                fcmTokenCommandService.updateTopic(
                    TopicUpdateCommand(fcmToken = token.token, topicType = TopicType.MAJOR, topicId = 300, enabled = false)
                ) shouldBe true
                verify(exactly = 1) {
                    fcmTokenPersistencePort.unsubscribe(1L, 300)
                }
            }
        }

        When("v1 토픽 이름이 요청한 유형에 속하지 않는 경우") {
            clearMocks(fcmTokenPersistencePort)
            every {
                fcmTokenPersistencePort.getByToken(token.token)
            } returns token

            Then("TopicException 이 발생한다") {
                shouldThrow<TopicException> {
                    fcmTokenCommandService.updateTopic(
                        TopicUpdateCommand(fcmToken = token.token, topicType = TopicType.MAJOR, topicName = "GENERAL_NEWS", enabled = true)
                    )
                }
            }
        }

        When("존재하지 않는 토픽 코드인 경우") {
            clearMocks(fcmTokenPersistencePort)
            every {
                fcmTokenPersistencePort.getByToken(token.token)
            } returns token

            Then("TopicException 이 발생한다") {
                shouldThrow<TopicException> {
                    fcmTokenCommandService.updateTopic(
                        TopicUpdateCommand(fcmToken = token.token, topicType = TopicType.NOTICE, topicId = 9999, enabled = true)
                    )
                }
            }
        }
    }

})
