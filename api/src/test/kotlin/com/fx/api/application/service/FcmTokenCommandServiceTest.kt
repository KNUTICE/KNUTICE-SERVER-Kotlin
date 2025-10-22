package com.fx.api.application.service

import com.fx.api.application.port.`in`.dto.FcmTokenSaveCommand
import com.fx.api.application.port.`in`.dto.FcmTokenUpdateCommand
import com.fx.api.application.port.`in`.dto.TopicUpdateCommand
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.global.exception.FcmTokenException
import com.fx.global.exception.errorcode.FcmTokenErrorCode
import com.fx.global.domain.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*

class FcmTokenCommandServiceTest : BehaviorSpec({

    val fcmTokenPersistencePort = mockk<FcmTokenPersistencePort>(relaxed = true)
    val fcmTokenCommandService = FcmTokenCommandService(fcmTokenPersistencePort)

    Given("새로운 토큰 저장") {
        val fcmTokenSaveCommand = FcmTokenSaveCommand(
            fcmToken = "newFcmToken",
            deviceType = DeviceType.iOS
        )

        When("존재하지 않은 토큰인 경우") {
            clearMocks(fcmTokenPersistencePort)
            every {
                fcmTokenPersistencePort.findByFcmToken(fcmTokenSaveCommand.fcmToken)
            } returns null

            Then("새로운 FCM 토큰이 생성돼 저장") {
                val result = fcmTokenCommandService.saveFcmToken(fcmTokenSaveCommand)
                result shouldBe true

                verify(exactly = 1) {
                    fcmTokenPersistencePort.saveFcmToken(match {
                        it.fcmToken == fcmTokenSaveCommand.fcmToken && it.isActive
                    })
                }
            }
        }

        When("이미 토큰이 존재하는 경우") {
            clearMocks(fcmTokenPersistencePort)
            val existingToken = FcmToken.createFcmToken(
                fcmTokenSaveCommand.fcmToken,
                fcmTokenSaveCommand.deviceType
            ).copy(isActive = false)
            every {
                fcmTokenPersistencePort.findByFcmToken(fcmTokenSaveCommand.fcmToken)
            } returns existingToken

            Then("기존 토큰의 isActive 가 true 로 업데이트된다") {
                val result = fcmTokenCommandService.saveFcmToken(fcmTokenSaveCommand)
                result shouldBe true

                verify(exactly = 1) {
                    fcmTokenPersistencePort.saveFcmToken(match {
                        it.fcmToken == fcmTokenSaveCommand.fcmToken && it.isActive
                    })
                }
            }
        }

    }

    Given("Silent Push 요청으로 토큰이 업데이트되는 경우") {
        val fcmTokenUpdateCommand = FcmTokenUpdateCommand(
            oldFcmToken = "oldFcmToken",
            newFcmToken = "newFcmToken",
            deviceType = DeviceType.iOS
        )

        When("oldFcmToken 이 존재하는 경우") {
            clearMocks(fcmTokenPersistencePort)
            val oldFcmToken = FcmToken.createFcmToken(
                fcmTokenUpdateCommand.oldFcmToken,
                fcmTokenUpdateCommand.deviceType
            )
            every {
                fcmTokenPersistencePort.findByFcmToken(fcmTokenUpdateCommand.oldFcmToken)
            } returns oldFcmToken

            Then("oldFcmToken 은 isActive=false, newFcmToken 은 저장") {
                val result = fcmTokenCommandService.updateFcmToken(fcmTokenUpdateCommand)
                result shouldBe true

                verifyOrder {
                    fcmTokenPersistencePort.saveFcmToken(match {
                        !it.isActive && it.fcmToken == fcmTokenUpdateCommand.oldFcmToken
                    })
                    fcmTokenPersistencePort.saveFcmToken(match {
                        it.fcmToken == fcmTokenUpdateCommand.newFcmToken && it.isActive
                    })
                }
            }
        }

        When("oldFcmToken 이 존재하지 않는 경우") {
            clearMocks(fcmTokenPersistencePort)
            every {
                fcmTokenPersistencePort.findByFcmToken(fcmTokenUpdateCommand.oldFcmToken)
            } returns null

            Then("새로운 newFcmToken 생성") {
                val result = fcmTokenCommandService.updateFcmToken(fcmTokenUpdateCommand)
                result shouldBe true

                verify(exactly = 1) {
                    fcmTokenPersistencePort.saveFcmToken(match {
                        it.fcmToken == fcmTokenUpdateCommand.newFcmToken && it.isActive
                    })
                }
            }
        }

    }

    Given("Topic 변경") {
        val fcmTokenStr = "fcmToken"
        val noticeTopicUpdateCommand = TopicUpdateCommand(
            fcmToken = fcmTokenStr,
            topicType = TopicType.NOTICE,
            topic = NoticeType.GENERAL_NEWS,
            enabled = false
        )

        val mealTopicUpdateCommand = TopicUpdateCommand(
            fcmToken = fcmTokenStr,
            topicType = TopicType.MEAL,
            topic = MealType.STUDENT_CAFETERIA,
            enabled = false
        )

        val fcmToken = FcmToken.createFcmToken(
            fcmTokenStr,
            DeviceType.iOS
        )

        When("Fcm token 이 존재하지 않는 경우") {
            every {
                fcmTokenPersistencePort.findByFcmToken(fcmTokenStr)
            } returns null

            Then("FcmTokenException 예외 발생") {
                val exception = shouldThrow<FcmTokenException> {
                    fcmTokenCommandService.updateTopic(noticeTopicUpdateCommand)
                }

                exception.baseErrorCode shouldBe FcmTokenErrorCode.TOKEN_NOT_FOUND
            }
        }

        When("Fcm token 이 존재하는 경유") {
            clearMocks(fcmTokenPersistencePort)
            every {
                fcmTokenPersistencePort.findByFcmToken(noticeTopicUpdateCommand.fcmToken)
            } returns fcmToken

            Then("GENERAL_NEWS Topic 제거") {
                val result = fcmTokenCommandService.updateTopic(noticeTopicUpdateCommand)
                result shouldBe true

                verify(exactly = 1) {
                    fcmTokenPersistencePort.saveFcmToken(match {
                        it.fcmToken == noticeTopicUpdateCommand.fcmToken && !it.subscribedNoticeTopics.contains(
                            NoticeType.GENERAL_NEWS)
                    })
                }
            }

            // TODO : 앱 기능 추가 후 활성화
//            Then("STUDENT_CAFETERIA Topic 제거") {
//                val result = fcmTokenCommandService.updateTopic(mealTopicUpdateCommand)
//                result shouldBe true
//
//                verify(exactly = 1) {
//                    fcmTokenPersistencePort.saveFcmToken(match {
//                        it.fcmToken == mealTopicUpdateCommand.fcmToken && !it.subscribedMealTopics.contains(
//                            MealType.STUDENT_CAFETERIA)
//                    })
//                }
//            }
        }
    }


})