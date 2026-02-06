package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.document.ApiLogDocument
import com.fx.api.adapter.out.persistence.repository.ApiLogMongoRepository
import com.fx.api.application.port.out.ApiLogPersistencePort
import com.fx.api.domain.ApiLog
import com.fx.global.annotation.PersistenceAdapter

@PersistenceAdapter
class ApiLogPersistenceAdapter(
    private val apiLogMongoRepository: ApiLogMongoRepository
): ApiLogPersistencePort {

    override fun save(apiLog: ApiLog) {
        apiLogMongoRepository.save(ApiLogDocument.from(apiLog))
    }

}