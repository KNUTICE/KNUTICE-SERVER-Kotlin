package com.fx.api.application.port.out.report

import com.fx.api.domain.Report

interface ReportPersistencePort {

    fun save(report: Report): Report

}
