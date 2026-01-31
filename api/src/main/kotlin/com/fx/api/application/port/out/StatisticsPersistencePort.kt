package com.fx.api.application.port.out

import com.fx.global.domain.DailyStatistics
import org.springframework.data.domain.Pageable
import java.time.LocalDate

interface StatisticsPersistencePort {

    fun findAllByDateLessThan(date: LocalDate, pageable: Pageable): List<DailyStatistics>

}