package com.fx.api.application.service.report

import com.fx.api.application.port.`in`.report.ReportCommandUseCase
import com.fx.api.application.port.`in`.report.dto.ReportSaveCommand
import com.fx.api.application.port.out.fcmtoken.FcmTokenPersistencePort
import com.fx.api.application.port.out.report.ReportPersistencePort
import com.fx.api.domain.Report
import com.fx.common.application.port.out.WebhookPort
import com.fx.common.domain.SlackMessage
import com.fx.common.domain.SlackType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ReportCommandService(
    private val reportPersistencePort: ReportPersistencePort,
    private val fcmTokenPersistencePort: FcmTokenPersistencePort,
    private val webhookPort: WebhookPort,
) : ReportCommandUseCase {

    /** 문의를 저장하고 Slack 으로 알린다. Slack 전송은 기다리지 않는다. */
    @Transactional
    override fun saveReport(reportSaveCommand: ReportSaveCommand): Boolean {
        val fcmToken = fcmTokenPersistencePort.getByToken(reportSaveCommand.fcmToken)

        val report = reportPersistencePort.save(
            Report(
                fcmTokenId = requireNotNull(fcmToken.id),
                content = reportSaveCommand.content,
                deviceName = reportSaveCommand.deviceName,
                version = reportSaveCommand.version,
            )
        )
        webhookPort.notifySlack(createSlackMessage(report))
        return true
    }

    private fun createSlackMessage(report: Report): SlackMessage =
        SlackMessage.create(
            """
                *내용* : ${report.content}
                *디바이스* : ${report.deviceName}
                *버전* : ${report.version}
                *날짜* : ${report.createdAt}
            """.trimIndent(),
            SlackType.REPORT
        )

}
