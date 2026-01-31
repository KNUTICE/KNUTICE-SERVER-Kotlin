package com.fx.crawler.appllication.port.out

import com.fx.global.domain.DailyStatistics

interface StatisticsPersistencePort {

    fun save(dailyStatistics: DailyStatistics)

}