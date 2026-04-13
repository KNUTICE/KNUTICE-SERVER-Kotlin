package com.fx.api.adapter.out.persistence.repository;

import com.fx.api.adapter.out.persistence.document.ReportDocument
import org.springframework.data.mongodb.repository.MongoRepository

interface ReportMongoRepository : MongoRepository<ReportDocument, String> {

}
