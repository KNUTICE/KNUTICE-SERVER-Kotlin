package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.StatisticsMongoRepository
import com.fx.api.application.port.out.StatisticsPersistencePort
import com.fx.global.annotation.PersistenceAdapter
import com.fx.global.domain.DailyStatistics
import org.springframework.data.domain.Pageable
import java.time.LocalDate

@PersistenceAdapter
class StatisticsPersistenceAdapter(
    private val dailyStatisticsRepository: StatisticsMongoRepository
) : StatisticsPersistencePort {

    override fun findAllByDateLessThan(date: LocalDate, pageable: Pageable): List<DailyStatistics> =
        dailyStatisticsRepository.findByStatisticsDateLessThan(date, pageable)
            .map { it.toDomain() }

}