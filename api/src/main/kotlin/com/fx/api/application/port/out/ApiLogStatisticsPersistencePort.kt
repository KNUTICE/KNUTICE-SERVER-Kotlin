package com.fx.api.application.port.out

import com.fx.common.domain.DailyApiLogStatistics
import org.springframework.data.domain.Pageable
import java.time.LocalDate

interface ApiLogStatisticsPersistencePort {

    fun findAllByDateLessThan(date: LocalDate, pageable: Pageable): List<DailyApiLogStatistics>

}