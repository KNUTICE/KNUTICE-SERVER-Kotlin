package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.ApiLogRepository
import com.fx.api.application.port.out.apilog.ApiLogPersistencePort
import com.fx.api.domain.ApiLog
import com.fx.common.annotation.PersistenceAdapter

@PersistenceAdapter
class ApiLogPersistenceAdapter(
    private val apiLogRepository: ApiLogRepository,
) : ApiLogPersistencePort {

    override fun saveAll(apiLogs: List<ApiLog>) {
        apiLogRepository.saveAll(apiLogs)
    }

}
