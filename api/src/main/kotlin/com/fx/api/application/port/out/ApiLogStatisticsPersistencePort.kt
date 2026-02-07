package com.fx.api.application.port.out

import com.fx.global.domain.DailyApiLogStatistics
import org.springframework.data.domain.Pageable
import java.time.LocalDate

interface ApiLogStatisticsPersistencePort {

    fun findAllByDateLessThan(date: LocalDate, pageable: Pageable): List<DailyApiLogStatistics>

}