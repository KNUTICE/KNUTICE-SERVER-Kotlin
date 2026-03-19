package com.fx.crawler.appllication.port.out

import com.fx.global.domain.DailyStatistics

interface StatisticsPersistencePort {

    suspend fun save(dailyStatistics: DailyStatistics)

}