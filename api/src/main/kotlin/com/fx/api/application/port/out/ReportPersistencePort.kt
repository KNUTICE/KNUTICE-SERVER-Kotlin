package com.fx.api.application.port.out

import com.fx.api.domain.Report

interface ReportPersistencePort {

    suspend fun saveReport(report: Report): Report

}