package com.fx.crawler.application.port.`in`

import com.fx.crawler.domain.statistics.AggregationPlan
import java.time.LocalDate

interface StatisticsAggregationUseCase {

    /** 오늘(KST)을 기준으로 어제부터 [lookbackDays] 일을 집계 대상으로 정한다. */
    fun plan(lookbackDays: Int): AggregationPlan

    /**
     * [date] 하루를 다시 집계해 통계를 덮어쓰고 쓴 통계 행 수를 돌려준다.
     * @param captureSnapshot 토큰 현황을 함께 기록할지. 지금 상태라 그날 마감 직후(어제)에만 맞는 값이다.
     */
    fun aggregate(date: LocalDate, captureSnapshot: Boolean): Int

    /** 보관 기간이 지난 API 로그를 [limit] 개까지 지우고 지운 수를 돌려준다. */
    fun deleteExpiredApiLogs(retentionDays: Int, limit: Int): Int

}
