package com.fx.api.adapter.out.persistence.repository;

import com.fx.api.adapter.out.persistence.document.ApiLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ApiLogMongoRepository extends MongoRepository<ApiLogDocument, String> {

}
