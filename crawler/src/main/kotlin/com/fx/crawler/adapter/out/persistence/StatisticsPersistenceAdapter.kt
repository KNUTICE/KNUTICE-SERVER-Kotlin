package com.fx.crawler.adapter.out.persistence

import com.fx.crawler.adapter.out.persistence.repository.StatisticsMongoRepository
import com.fx.crawler.appllication.port.out.StatisticsPersistencePort
import com.fx.global.adapter.out.persistence.document.DailyStatisticsDocument
import com.fx.global.annotation.PersistenceAdapter
import com.fx.global.domain.DailyStatistics

@PersistenceAdapter
class StatisticsPersistenceAdapter(
    private val statisticsMongoRepository: StatisticsMongoRepository
): StatisticsPersistencePort {

    override fun save(dailyStatistics: DailyStatistics) {
        statisticsMongoRepository.save(DailyStatisticsDocument.from(dailyStatistics))
    }

}