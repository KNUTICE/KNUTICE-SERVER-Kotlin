package com.fx.api.adapter.out.persistence.repository

import com.fx.common.adapter.out.persistence.document.ApiLogDocument
import org.springframework.data.mongodb.repository.MongoRepository;

interface ApiLogMongoRepository : MongoRepository<ApiLogDocument, String> {

}
