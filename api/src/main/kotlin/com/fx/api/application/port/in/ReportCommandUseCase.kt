package com.fx.api.application.port.`in`

import com.fx.api.application.port.`in`.dto.ReportSaveCommand

interface ReportCommandUseCase {

    suspend fun saveReport(reportSaveCommand: ReportSaveCommand): Boolean

}