package com.fx.crawler.appllication.port.out

import com.fx.global.domain.DailyApiLogStatistics
import java.time.LocalDate

interface ApiLogPersistencePort {

    fun aggregateDailyStatistics(date: LocalDate): List<DailyApiLogStatistics>

}