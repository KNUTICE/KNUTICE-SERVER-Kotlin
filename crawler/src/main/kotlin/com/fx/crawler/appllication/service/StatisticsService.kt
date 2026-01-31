package com.fx.crawler.appllication.service

import com.fx.crawler.appllication.port.`in`.StatisticsUseCase
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class StatisticsService(
) : StatisticsUseCase {

    private val log = LoggerFactory.getLogger(StatisticsService::class.java)

    override suspend fun aggregateDailyStatistics() {
        TODO("Not yet implemented")
    }

}