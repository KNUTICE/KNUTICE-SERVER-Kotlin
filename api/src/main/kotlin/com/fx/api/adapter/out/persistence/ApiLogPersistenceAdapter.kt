package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.ApiLogQueryRepository
import com.fx.api.adapter.out.persistence.repository.ApiLogRepository
import com.fx.api.application.port.out.apilog.ApiLogPersistencePort
import com.fx.api.domain.ApiLog
import com.fx.api.domain.ApiLogEntry
import com.fx.api.domain.ApiLogStatusGroup
import com.fx.common.annotation.PersistenceAdapter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate

@PersistenceAdapter
class ApiLogPersistenceAdapter(
    private val apiLogRepository: ApiLogRepository,
    private val apiLogQueryRepository: ApiLogQueryRepository,
) : ApiLogPersistencePort {

    override fun saveAll(apiLogs: List<ApiLog>) {
        apiLogRepository.saveAll(apiLogs)
    }

    override fun findApiLogs(
        date: LocalDate,
        urlPattern: String?,
        statusGroup: ApiLogStatusGroup?,
        pageable: Pageable,
    ): Page<ApiLogEntry> =
        apiLogQueryRepository.findApiLogs(date, urlPattern, statusGroup, pageable)

}
