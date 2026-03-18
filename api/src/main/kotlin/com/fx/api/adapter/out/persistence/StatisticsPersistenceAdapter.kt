package com.fx.api.adapter.out.persistence

import com.fx.api.application.port.out.StatisticsPersistencePort
import com.fx.global.adapter.out.persistence.repository.StatisticsMongoRepository
import com.fx.global.annotation.PersistenceAdapter
import com.fx.global.domain.DailyStatistics
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Pageable
import java.time.LocalDate

@PersistenceAdapter
class StatisticsPersistenceAdapter(
    private val dailyStatisticsRepository: StatisticsMongoRepository
) : StatisticsPersistencePort {

    override suspend fun findAllByDateLessThan(date: LocalDate, pageable: Pageable): List<DailyStatistics> =
        dailyStatisticsRepository.findByStatisticsDateLessThan(date, pageable)
            .map { it.toDomain() }
            .toList()

}