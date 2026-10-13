package com.fx.api.application.port.`in`.apilog

import com.fx.api.domain.ApiLogEntry
import com.fx.api.domain.ApiLogStatusGroup
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate

interface ApiLogQueryUseCase {

    /** [date] 하루치 원본 API 로그를 최신순으로 조회한다. [date] 가 없으면 오늘(KST), 나머지 조건이 없으면 거르지 않는다. */
    fun getApiLogs(date: LocalDate?, urlPattern: String?, statusGroup: ApiLogStatusGroup?, pageable: Pageable): Page<ApiLogEntry>

}
