package com.fx.api.application.service.report

import com.fx.api.application.port.`in`.report.dto.ReportSaveCommand
import com.fx.api.application.port.out.fcmtoken.FcmTokenPersistencePort
import com.fx.api.application.port.out.report.ReportPersistencePort
import com.fx.api.domain.Report
import com.fx.common.application.port.out.WebhookPort
import com.fx.common.domain.DeviceType
import com.fx.common.domain.SlackType
import com.fx.common.domain.fcmtoken.FcmToken
import com.fx.common.exception.FcmTokenException
import com.fx.common.exception.errorcode.FcmTokenErrorCode
import com.fx.persistence.withId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

class ReportCommandServiceTest : BehaviorSpec({

    val reportPersistencePort = mockk<ReportPersistencePort>()
    val fcmTokenPersistencePort = mockk<FcmTokenPersistencePort>()
    val webhookPort = mockk<WebhookPort>(relaxed = true)
    val reportCommandService = ReportCommandService(reportPersistencePort, fcmTokenPersistencePort, webhookPort)

    val command = ReportSaveCommand(fcmToken = "fcmToken", content = "문의 내용입니다", deviceName = "iPhone", version = "1.8.0")

    Given("문의사항 저장") {

        When("토큰이 존재하면") {
            clearMocks(reportPersistencePort, fcmTokenPersistencePort, webhookPort)
            every {
                fcmTokenPersistencePort.getByToken("fcmToken")
            } returns FcmToken("fcmToken", DeviceType.iOS).withId(1L)
            val saved = slot<Report>()
            every {
                reportPersistencePort.save(capture(saved))
            } answers {
                saved.captured
            }

            Then("토큰 ID 로 저장하고 Slack 으로 알린다") {
                reportCommandService.saveReport(command) shouldBe true
                saved.captured.fcmTokenId shouldBe 1L
                saved.captured.content shouldBe command.content
                verify(exactly = 1) {
                    webhookPort.notifySlack(match {
                        it.type == SlackType.REPORT
                    })
                }
            }
        }

        When("토큰이 존재하지 않으면") {
            clearMocks(reportPersistencePort, fcmTokenPersistencePort, webhookPort)
            every {
                fcmTokenPersistencePort.getByToken("fcmToken")
            } throws FcmTokenException(FcmTokenErrorCode.TOKEN_NOT_FOUND)

            Then("FcmTokenException 이 발생하고 저장 · 알림을 하지 않는다") {
                shouldThrow<FcmTokenException> {
                    reportCommandService.saveReport(command)
                }
                verify(exactly = 0) {
                    reportPersistencePort.save(any())
                }
                verify(exactly = 0) {
                    webhookPort.notifySlack(any())
                }
            }
        }
    }

})
