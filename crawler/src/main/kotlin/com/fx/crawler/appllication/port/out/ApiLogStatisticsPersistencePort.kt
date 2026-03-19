package com.fx.crawler.appllication.port.out

import com.fx.global.domain.DailyApiLogStatistics

interface ApiLogStatisticsPersistencePort {

    suspend fun saveAll(dailyApiLogStatistics: List<DailyApiLogStatistics>)

}