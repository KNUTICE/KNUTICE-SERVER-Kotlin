package com.fx.crawler.appllication.port.out

import com.fx.global.domain.DailyApiLogStatistics

interface ApiLogStatisticsPersistencePort {

    fun saveAll(dailyApiLogStatistics: List<DailyApiLogStatistics>)

}