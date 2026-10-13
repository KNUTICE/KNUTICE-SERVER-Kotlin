package com.fx.api.application.service.apilog

import com.fx.api.application.port.`in`.apilog.ApiLogQueryUseCase
import com.fx.api.application.port.out.apilog.ApiLogPersistencePort
import com.fx.api.domain.ApiLogEntry
import com.fx.api.domain.ApiLogStatusGroup
import com.fx.common.domain.statistics.StatisticsTime
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate

/** 원본 API 로그 조회. 보관 기간이 지난 로그는 crawler 의 일일 통계 Job 이 지운다. */
@Service
@Transactional(readOnly = true)
class ApiLogQueryService(
    private val apiLogPersistencePort: ApiLogPersistencePort,
    private val clock: Clock,
) : ApiLogQueryUseCase {

    override fun getApiLogs(
        date: LocalDate?,
        urlPattern: String?,
        statusGroup: ApiLogStatusGroup?,
        pageable: Pageable,
    ): Page<ApiLogEntry> =
        apiLogPersistencePort.findApiLogs(date ?: StatisticsTime.today(clock), urlPattern, statusGroup, pageable)

}
