package com.fx.api.application.port.out

import com.fx.api.domain.DailyTopicCount
import com.fx.global.domain.ApiLog
import java.time.LocalDateTime

interface ApiLogPersistencePort {

    suspend fun save(apiLog: ApiLog)

    suspend fun aggregateDailyTopicStatistics(start: LocalDateTime, end: LocalDateTime): List<DailyTopicCount>

}