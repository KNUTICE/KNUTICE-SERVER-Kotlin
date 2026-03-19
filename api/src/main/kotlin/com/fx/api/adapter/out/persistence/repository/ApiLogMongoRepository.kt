package com.fx.api.adapter.out.persistence.repository

import com.fx.global.adapter.out.persistence.document.ApiLogDocument
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ApiLogMongoRepository : CoroutineCrudRepository<ApiLogDocument, String> {

}
