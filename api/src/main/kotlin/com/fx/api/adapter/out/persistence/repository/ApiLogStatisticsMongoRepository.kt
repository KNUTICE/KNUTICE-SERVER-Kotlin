package com.fx.api.adapter.out.persistence.repository;

import com.fx.global.adapter.out.persistence.document.DailyApiLogStatisticsDocument
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.LocalDate
import java.util.List;

interface ApiLogStatisticsMongoRepository : MongoRepository<DailyApiLogStatisticsDocument, String> {

    fun findByStatisticsDateLessThan(date: LocalDate, pageable: Pageable): List<DailyApiLogStatisticsDocument>

}
