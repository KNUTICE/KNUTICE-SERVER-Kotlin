package com.fx.api.application.service

import com.fx.api.application.port.`in`.ReportCommandUseCase
import com.fx.api.application.port.`in`.dto.ReportSaveCommand
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.api.application.port.out.ReportPersistencePort
import com.fx.api.domain.Report
import com.fx.api.exception.FcmTokenException
import com.fx.api.exception.errorcode.FcmTokenErrorCode
import org.springframework.stereotype.Service

@Service
class ReportCommandService(
    private val reportPersistencePort: ReportPersistencePort,
    private val fcmTokenPersistencePort: FcmTokenPersistencePort
): ReportCommandUseCase {

    override fun saveReport(reportSaveCommand: ReportSaveCommand): Boolean {
        if (!fcmTokenPersistencePort.existsByFcmToken(reportSaveCommand.fcmToken)) {
            throw FcmTokenException(FcmTokenErrorCode.TOKEN_NOT_FOUND)
        }

        reportPersistencePort.saveReport(Report.createReport(reportSaveCommand))
        return true
    }

}