package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.ApiLogStatisticsMongoRepository
import com.fx.api.application.port.out.ApiLogStatisticsPersistencePort
import com.fx.global.annotation.PersistenceAdapter
import com.fx.global.domain.DailyApiLogStatistics
import org.springframework.data.domain.Pageable
import java.time.LocalDate

@PersistenceAdapter
class ApiLogStatisticsPersistenceAdapter(
    private val apiLogStatisticsMongoRepository: ApiLogStatisticsMongoRepository
): ApiLogStatisticsPersistencePort {

    override fun findAllByDateLessThan(date: LocalDate, pageable: Pageable): List<DailyApiLogStatistics> =
        apiLogStatisticsMongoRepository.findByStatisticsDateLessThan(date, pageable)
            .map { it.toDomain() }

}