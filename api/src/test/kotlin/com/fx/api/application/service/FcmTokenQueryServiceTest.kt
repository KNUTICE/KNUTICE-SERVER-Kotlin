package com.fx.api.application.service

import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.global.domain.*
import com.fx.global.exception.FcmTokenException
import com.fx.global.exception.errorcode.FcmTokenErrorCode
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class FcmTokenQueryServiceTest : BehaviorSpec({

    val fcmTokenPersistencePort = mockk<FcmTokenPersistencePort>(relaxed = true)
    val fcmTokenQueryService = FcmTokenQueryService(fcmTokenPersistencePort)

    val fcmTokenValue = "fcmToken"
    val fcmToken = FcmToken(
        fcmToken = fcmTokenValue,
        subscribedNoticeTopics = setOf(NoticeType.GENERAL_NEWS, NoticeType.SCHOLARSHIP_NEWS),
        subscribedMajorTopics = setOf(MajorType.COMPUTER_SCIENCE, MajorType.COMPUTER_ENGINEERING),
        subscribedMealTopics = setOf(MealType.STUDENT_CAFETERIA, MealType.STAFF_CAFETERIA),
        deviceType = DeviceType.iOS,
        isActive = true
    )

    Given("토픽 조회") {
        every { fcmTokenPersistencePort.findByFcmToken(fcmTokenValue) } returns fcmToken

        When("Notice 토픽 조회") {
            val result = fcmTokenQueryService.getMyTopics(fcmTokenValue, TopicType.NOTICE)

            Then("Notice 토픽만 반환") {
                result shouldBe setOf(NoticeType.GENERAL_NEWS.name, NoticeType.SCHOLARSHIP_NEWS.name)
            }
        }

        When("Major 토픽 조회") {
            val result = fcmTokenQueryService.getMyTopics(fcmTokenValue, TopicType.MAJOR)

            Then("Major 토픽만 반환") {
                result shouldBe setOf(MajorType.COMPUTER_SCIENCE.name, MajorType.COMPUTER_ENGINEERING.name)
            }
        }

        When("Meal 토픽 조회") {
            val result = fcmTokenQueryService.getMyTopics(fcmTokenValue, TopicType.MEAL)

            Then("Meal 토픽만 반환") {
                result shouldBe setOf(MealType.STUDENT_CAFETERIA.name, MealType.STAFF_CAFETERIA.name)
            }
        }
    }

    Given("토큰이 존재하지 않은 경우") {
        every { fcmTokenPersistencePort.findByFcmToken(fcmTokenValue) } returns null

        When("토픽을 조회") {
            Then("예외가 발생") {
                val exception = shouldThrow<FcmTokenException> {
                    fcmTokenQueryService.getMyTopics(fcmTokenValue, TopicType.NOTICE)
                }
                exception.baseErrorCode shouldBe FcmTokenErrorCode.TOKEN_NOT_FOUND
            }
        }
    }


})