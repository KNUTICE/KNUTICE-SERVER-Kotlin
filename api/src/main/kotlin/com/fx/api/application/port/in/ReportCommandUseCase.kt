package com.fx.api.application.port.`in`

import com.fx.api.application.port.`in`.dto.ReportSaveCommand

interface ReportCommandUseCase {

    fun saveReport(reportSaveCommand: ReportSaveCommand): Boolean

}