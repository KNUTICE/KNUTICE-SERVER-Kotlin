package com.fx.api.application.port.out.apilog

import com.fx.api.domain.ApiLog
import com.fx.api.domain.ApiLogEntry
import com.fx.api.domain.ApiLogStatusGroup
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate

interface ApiLogPersistencePort {

    /** 묶어서 저장한다 (JDBC 배치 INSERT). */
    fun saveAll(apiLogs: List<ApiLog>)

    /** [date] 하루치를 요청 시각 · ID 내림차순으로. [urlPattern] · [statusGroup] 이 없으면 거르지 않는다. */
    fun findApiLogs(date: LocalDate, urlPattern: String?, statusGroup: ApiLogStatusGroup?, pageable: Pageable): Page<ApiLogEntry>

}
