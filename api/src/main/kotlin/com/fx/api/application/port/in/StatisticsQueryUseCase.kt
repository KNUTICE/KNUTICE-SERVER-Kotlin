package com.fx.api.application.port.`in`

import com.fx.global.domain.DailyStatistics
import java.time.LocalDate

interface StatisticsQueryUseCase {

    suspend fun getDailyStatistics(cursorDate: LocalDate?, size: Int): List<DailyStatistics>

}