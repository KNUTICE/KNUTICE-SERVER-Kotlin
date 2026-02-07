package com.fx.crawler.adapter.out.persistence

import com.fx.crawler.adapter.out.persistence.repository.ApiLogStatisticsMongoRepository
import com.fx.crawler.adapter.out.persistence.repository.StatisticsMongoRepository
import com.fx.crawler.appllication.port.out.ApiLogStatisticsPersistencePort
import com.fx.crawler.appllication.port.out.StatisticsPersistencePort
import com.fx.global.adapter.out.persistence.document.DailyApiLogStatisticsDocument
import com.fx.global.adapter.out.persistence.document.DailyStatisticsDocument
import com.fx.global.annotation.PersistenceAdapter
import com.fx.global.domain.DailyApiLogStatistics
import com.fx.global.domain.DailyStatistics

@PersistenceAdapter
class ApiLogStatisticsPersistenceAdapter(
    private val apiLogStatisticsMongoRepository: ApiLogStatisticsMongoRepository
): ApiLogStatisticsPersistencePort {

    override fun saveAll(dailyApiLogStatistics: List<DailyApiLogStatistics>) {
        apiLogStatisticsMongoRepository.saveAll(
            dailyApiLogStatistics.map { DailyApiLogStatisticsDocument.from(it) }
        )
    }

}