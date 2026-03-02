package com.fx.api.adapter.out.persistence.repository

import com.fx.global.adapter.out.persistence.document.ApiLogDocument
import org.springframework.data.mongodb.repository.MongoRepository;

interface ApiLogMongoRepository : MongoRepository<ApiLogDocument, String> {

}
