package com.fx.crawler.application.service

import com.fx.common.domain.statistics.StatisticsTime
import com.fx.crawler.application.port.`in`.StatisticsAggregationUseCase
import com.fx.crawler.application.port.out.StatisticsPersistencePort
import com.fx.crawler.domain.statistics.AggregationPlan
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 일일 통계 집계. 날짜는 API 로그의 요청 시각과 같은 KST 기준이다.
 * 날짜 D 는 `[D 00:00, D+1 00:00)` 로 골라 자정 정각 요청이 두 날짜에 들어가거나 빠지지 않게 한다.
 */
@Service
@Transactional(readOnly = true)
class StatisticsAggregationService(
    private val statisticsPersistencePort: StatisticsPersistencePort,
    private val clock: Clock,
) : StatisticsAggregationUseCase {

    override fun plan(lookbackDays: Int): AggregationPlan =
        AggregationPlan.of(StatisticsTime.today(clock), lookbackDays)

    @Transactional
    override fun aggregate(date: LocalDate, captureSnapshot: Boolean): Int {
        val from =
            date.atStartOfDay()
        val to =
            date.plusDays(1).atStartOfDay()
        val apiLogStatistics =
            statisticsPersistencePort.aggregateApiLogs(from, to)
        val topicStatistics =
            statisticsPersistencePort.aggregateTopics(from, to)
        val totals =
            statisticsPersistencePort.aggregateTotals(from, to)

        val now =
            LocalDateTime.now(clock)
        statisticsPersistencePort.saveApiLogStatistics(date, apiLogStatistics, now)
        statisticsPersistencePort.saveTopicStatistics(date, topicStatistics, now)
        statisticsPersistencePort.saveTotals(date, totals, now)
        if (captureSnapshot) {
            statisticsPersistencePort.saveTokenSnapshot(date, statisticsPersistencePort.captureTokenSnapshot(), now)
        }
        return apiLogStatistics.size + topicStatistics.size + 1
    }

    @Transactional
    override fun deleteExpiredApiLogs(retentionDays: Int, limit: Int): Int {
        require(retentionDays >= 1) {
            "API 로그 보관 기간은 1일 이상이어야 합니다: $retentionDays"
        }
        val cutoff =
            StatisticsTime.today(clock).minusDays(retentionDays.toLong()).atStartOfDay()
        return statisticsPersistencePort.deleteApiLogsRequestedBefore(cutoff, limit)
    }

}
