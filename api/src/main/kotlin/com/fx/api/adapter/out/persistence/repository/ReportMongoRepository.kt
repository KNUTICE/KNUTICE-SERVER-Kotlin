package com.fx.api.adapter.out.persistence.repository;

import com.fx.api.adapter.out.persistence.document.ReportDocument
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ReportMongoRepository : CoroutineCrudRepository<ReportDocument, String> {

}
