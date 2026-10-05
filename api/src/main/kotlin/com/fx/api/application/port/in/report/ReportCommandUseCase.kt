package com.fx.api.application.port.`in`.report

import com.fx.api.application.port.`in`.report.dto.ReportSaveCommand

interface ReportCommandUseCase {

    fun saveReport(reportSaveCommand: ReportSaveCommand): Boolean

}
