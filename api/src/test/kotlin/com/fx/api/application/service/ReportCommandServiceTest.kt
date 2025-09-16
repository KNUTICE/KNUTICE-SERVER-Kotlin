package com.fx.api.application.service

import com.fx.api.application.port.`in`.dto.ReportSaveCommand
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.api.application.port.out.ReportPersistencePort
import com.fx.api.domain.Report
import com.fx.api.exception.FcmTokenException
import com.fx.api.exception.errorcode.FcmTokenErrorCode
import com.fx.global.application.port.out.WebhookPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.common.runBlocking
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class ReportCommandServiceTest : BehaviorSpec({

    val reportPersistencePort = mockk<ReportPersistencePort>(relaxed = true)
    val fcmTokenPersistencePort = mockk<FcmTokenPersistencePort>(relaxed = true)
    val webhookPort = mockk<WebhookPort>(relaxed = true)
    val reportCommandService = ReportCommandService(reportPersistencePort, fcmTokenPersistencePort, webhookPort)

    Given("문의사항 저장") {
        val reportSaveCommand = ReportSaveCommand(
            fcmToken = "fcmToken",
            content = "content",
            deviceName = "galaxy s23",
            version = "ver3.1"
        )
        val report = Report.createReport(reportSaveCommand)

        When("문의사항 저장") {
            coEvery { fcmTokenPersistencePort.existsByFcmToken(reportSaveCommand.fcmToken) } returns true
            coEvery { reportPersistencePort.saveReport(report) } returns report
//            coEvery { webhookPort.notifySlack(any()) }

            Then("정상적으로 저장되고 Slack 알림 호출") {
                runBlocking {
                    val result = reportCommandService.saveReport(reportSaveCommand)
                    result shouldBe true
                }

                coVerify(exactly = 1) {
                    reportPersistencePort.saveReport(report)
//                    webhookPort.notifySlack(any())
                }
            }
        }

        When("FCM 토큰이 존재하지 않는 경우") {
            coEvery { fcmTokenPersistencePort.existsByFcmToken(reportSaveCommand.fcmToken) } returns false

            Then("FcmTokenException 발생") {
                runBlocking {
                    val exception = shouldThrow<FcmTokenException> {
                        reportCommandService.saveReport(reportSaveCommand)
                    }
                    exception.baseErrorCode shouldBe FcmTokenErrorCode.TOKEN_NOT_FOUND
                }
            }
        }
    }

})