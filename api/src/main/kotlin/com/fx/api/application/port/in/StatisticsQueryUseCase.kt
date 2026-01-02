package com.fx.api.application.port.`in`

import com.fx.api.domain.Statistics

interface StatisticsQueryUseCase {

    suspend fun getStatistics(): Statistics

}