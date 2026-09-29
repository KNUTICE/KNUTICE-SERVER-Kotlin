package com.fx.crawler.appllication.port.out

import com.fx.common.domain.DailyApiLogStatistics

interface ApiLogStatisticsPersistencePort {

    fun saveAll(dailyApiLogStatistics: List<DailyApiLogStatistics>)

}