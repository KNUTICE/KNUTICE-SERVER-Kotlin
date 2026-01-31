package com.fx.crawler.adapter.`in`.scheduler

import com.fx.crawler.appllication.port.`in`.StatisticsUseCase
import com.fx.crawler.common.annotation.ScheduleAdapter
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import java.time.LocalDate

@ScheduleAdapter
class StatisticsScheduler(
    private val statisticsUseCase: StatisticsUseCase
) {

    private val log = LoggerFactory.getLogger(StatisticsScheduler::class.java)

    // 매일 00:05 분에 전날 데이터를 집계해 저장합니다.
    @Scheduled(cron = "0 5 0 * * *")
    fun aggregateDailyStatistics() = runBlocking {
        val yesterday = LocalDate.now().minusDays(1)
        log.info("---------- Starting scheduled daily statistics for $yesterday ----------")
        statisticsUseCase.aggregateDailyStatistics()
        log.info("---------- Scheduled daily statistics finished for $yesterday ----------")
    }

}