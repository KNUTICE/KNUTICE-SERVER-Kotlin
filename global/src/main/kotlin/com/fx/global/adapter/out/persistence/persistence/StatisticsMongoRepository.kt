package com.fx.global.adapter.out.persistence.persistence

import com.fx.global.adapter.out.persistence.document.DailyStatisticsDocument
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.LocalDate

interface StatisticsMongoRepository : MongoRepository<DailyStatisticsDocument, LocalDate> {

    fun findByStatisticsDateLessThan(
        date: LocalDate,
        pageable: Pageable
    ): List<DailyStatisticsDocument>


}