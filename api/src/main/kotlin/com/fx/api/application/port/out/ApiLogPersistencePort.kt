package com.fx.api.application.port.out

import com.fx.api.domain.DailyTopicCount
import com.fx.global.domain.ApiLog
import java.time.LocalDateTime

interface ApiLogPersistencePort {

    fun save(apiLog: ApiLog)

    fun aggregateDailyTopicStatistics(start: LocalDateTime, end: LocalDateTime): List<DailyTopicCount>

}