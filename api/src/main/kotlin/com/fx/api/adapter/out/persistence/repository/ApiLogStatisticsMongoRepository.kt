package com.fx.api.adapter.out.persistence.repository;

import com.fx.global.adapter.out.persistence.document.DailyApiLogStatisticsDocument
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.LocalDate

interface ApiLogStatisticsMongoRepository : CoroutineCrudRepository<DailyApiLogStatisticsDocument, String> {

    fun findByStatisticsDateLessThan(date: LocalDate, pageable: Pageable): Flow<DailyApiLogStatisticsDocument>

}
