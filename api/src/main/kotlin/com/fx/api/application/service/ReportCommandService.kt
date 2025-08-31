package com.fx.api.application.service

import com.fx.api.application.port.`in`.ReportCommandUseCase
import com.fx.api.application.port.`in`.dto.ReportCommand
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.api.application.port.out.ReportPersistencePort
import com.fx.api.domain.Report
import org.springframework.stereotype.Service

@Service
class ReportCommandService(
    private val reportPersistencePort: ReportPersistencePort,
    private val fcmTokenPersistencePort: FcmTokenPersistencePort
): ReportCommandUseCase {

    override fun saveReport(reportCommand: ReportCommand): Boolean {
        if (!fcmTokenPersistencePort.existsByFcmToken(reportCommand.fcmToken)) {
            throw RuntimeException("Token 이 존재하지 않음")
        }

        reportPersistencePort.saveReport(Report.createReport(reportCommand))
        return true
    }

}