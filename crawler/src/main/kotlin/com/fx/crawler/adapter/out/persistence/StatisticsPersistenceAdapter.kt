package com.fx.crawler.adapter.out.persistence

import com.fx.common.adapter.out.persistence.persistence.StatisticsMongoRepository
import com.fx.crawler.appllication.port.out.StatisticsPersistencePort
import com.fx.common.adapter.out.persistence.document.DailyStatisticsDocument
import com.fx.common.annotation.PersistenceAdapter
import com.fx.common.domain.DailyStatistics

@PersistenceAdapter
class StatisticsPersistenceAdapter(
    private val statisticsMongoRepository: StatisticsMongoRepository
): StatisticsPersistencePort {

    override fun save(dailyStatistics: DailyStatistics) {
        statisticsMongoRepository.save(DailyStatisticsDocument.from(dailyStatistics))
    }

}