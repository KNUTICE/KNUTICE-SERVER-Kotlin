package com.fx.global.adapter.out.persistence.repository

import com.fx.global.adapter.out.persistence.document.DailyStatisticsDocument
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.LocalDate

interface StatisticsMongoRepository : CoroutineCrudRepository<DailyStatisticsDocument, LocalDate> {

    fun findByStatisticsDateLessThan(
        date: LocalDate,
        pageable: Pageable
    ): Flow<DailyStatisticsDocument>


}