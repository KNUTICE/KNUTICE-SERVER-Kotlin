package com.fx.api.adapter.out.persistence.repository;

import com.fx.api.adapter.out.persistence.document.ReportDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReportMongoRepository extends MongoRepository<ReportDocument, String> {

}
