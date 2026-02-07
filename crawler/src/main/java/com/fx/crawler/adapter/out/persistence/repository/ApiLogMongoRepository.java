package com.fx.crawler.adapter.out.persistence.repository;

import com.fx.global.adapter.out.persistence.document.ApiLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ApiLogMongoRepository extends MongoRepository<ApiLogDocument, String> {

}
