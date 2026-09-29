package com.fx.crawler.appllication.port.out

import com.fx.common.domain.DailyStatistics

interface StatisticsPersistencePort {

    fun save(dailyStatistics: DailyStatistics)

}