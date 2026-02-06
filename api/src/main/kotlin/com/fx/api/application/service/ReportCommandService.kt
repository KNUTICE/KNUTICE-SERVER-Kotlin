package com.fx.api.application.service

import com.fx.api.application.port.`in`.ReportCommandUseCase
import com.fx.api.application.port.`in`.dto.ReportSaveCommand
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.api.application.port.out.ReportPersistencePort
import com.fx.api.domain.Report
import com.fx.global.exception.FcmTokenException
import com.fx.global.exception.errorcode.FcmTokenErrorCode
import com.fx.global.application.port.out.WebhookPort
import com.fx.global.domain.SlackMessage
import com.fx.global.domain.SlackType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service

@Service
class ReportCommandService(
    private val reportPersistencePort: ReportPersistencePort,
    private val fcmTokenPersistencePort: FcmTokenPersistencePort,
    private val webhookPort: WebhookPort
): ReportCommandUseCase {

    private val backgroundScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override suspend fun saveReport(reportSaveCommand: ReportSaveCommand): Boolean = coroutineScope {
        if (!fcmTokenPersistencePort.existsByFcmToken(reportSaveCommand.fcmToken)) {
            throw FcmTokenException(FcmTokenErrorCode.TOKEN_NOT_FOUND)
        }

        val savedReport = reportPersistencePort.saveReport(Report.createReport(reportSaveCommand))

        backgroundScope.launch {
            webhookPort.notifySlack(createSlackMessage(savedReport))
        }

        return@coroutineScope true
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