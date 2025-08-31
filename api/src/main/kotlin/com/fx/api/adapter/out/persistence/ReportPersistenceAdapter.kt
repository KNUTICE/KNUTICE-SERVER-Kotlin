package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.document.ReportDocument
import com.fx.api.adapter.out.persistence.repository.ReportMongoRepository
import com.fx.api.application.port.out.ReportPersistencePort
import com.fx.api.domain.Report
import com.fx.global.annotation.PersistenceAdapter

@PersistenceAdapter
class ReportPersistenceAdapter(
    private val reportMongoRepository: ReportMongoRepository
): ReportPersistencePort {

    override fun saveReport(report: Report) {
        reportMongoRepository.save(ReportDocument.from(report))
    }

}