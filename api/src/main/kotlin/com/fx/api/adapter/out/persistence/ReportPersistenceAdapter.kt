package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.ReportRepository
import com.fx.api.application.port.out.report.ReportPersistencePort
import com.fx.api.domain.Report
import com.fx.common.annotation.PersistenceAdapter

@PersistenceAdapter
class ReportPersistenceAdapter(
    private val reportRepository: ReportRepository,
) : ReportPersistencePort {

    override fun save(report: Report): Report =
        reportRepository.save(report)

}
