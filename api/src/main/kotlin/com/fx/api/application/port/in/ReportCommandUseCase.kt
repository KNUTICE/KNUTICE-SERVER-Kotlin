package com.fx.api.application.port.`in`

import com.fx.api.application.port.`in`.dto.ReportCommand

interface ReportCommandUseCase {

    fun saveReport(reportCommand: ReportCommand): Boolean

}