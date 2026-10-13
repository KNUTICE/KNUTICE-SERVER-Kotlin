package com.fx.crawler.application.port.out

import com.fx.crawler.domain.statistics.ApiLogStatistics
import com.fx.crawler.domain.statistics.DailyTotals
import com.fx.crawler.domain.statistics.TokenSnapshot
import com.fx.crawler.domain.statistics.TopicStatistics
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 일일 통계 집계 · 저장. 집계 범위는 반열린 구간 `[from, to)` 이다.
 * 저장은 날짜 · 집계 키가 같은 행이 있으면 값을 새 집계 결과로 바꾼다 (더하지 않는다).
 */
interface StatisticsPersistencePort {

    fun aggregateApiLogs(from: LocalDateTime, to: LocalDateTime): List<ApiLogStatistics>

    fun aggregateTopics(from: LocalDateTime, to: LocalDateTime): List<TopicStatistics>

    fun aggregateTotals(from: LocalDateTime, to: LocalDateTime): DailyTotals

    fun captureTokenSnapshot(): TokenSnapshot

    fun saveApiLogStatistics(date: LocalDate, statistics: List<ApiLogStatistics>, now: LocalDateTime)

    fun saveTopicStatistics(date: LocalDate, statistics: List<TopicStatistics>, now: LocalDateTime)

    /** 토큰 현황 컬럼은 건드리지 않는다. 새 행이면 비어 있다. */
    fun saveTotals(date: LocalDate, totals: DailyTotals, now: LocalDateTime)

    /** [saveTotals] 로 저장한 날짜에 토큰 현황을 기록한다. */
    fun saveTokenSnapshot(date: LocalDate, snapshot: TokenSnapshot, now: LocalDateTime)

    /** [cutoff] 이전에 들어온 API 로그를 [limit] 개까지 지우고 지운 수를 돌려준다. */
    fun deleteApiLogsRequestedBefore(cutoff: LocalDateTime, limit: Int): Int

}
