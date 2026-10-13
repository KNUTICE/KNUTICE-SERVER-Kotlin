package com.fx.crawler.domain.statistics

import java.time.LocalDate

/**
 * 한 번의 실행에서 집계할 날짜.
 * @property dates 어제부터 거슬러 올라간 날짜를 오래된 순으로 담는다
 * @property snapshotDate 토큰 현황을 기록할 날짜 (어제)
 */
data class AggregationPlan(
    val dates: List<LocalDate>,
    val snapshotDate: LocalDate,
) {

    companion object {

        fun of(today: LocalDate, lookbackDays: Int): AggregationPlan {
            require(lookbackDays >= 1) {
                "다시 집계할 일수는 1일 이상이어야 합니다: $lookbackDays"
            }
            val dates = (lookbackDays downTo 1).map {
                today.minusDays(it.toLong())
            }
            return AggregationPlan(dates, today.minusDays(1))
        }

    }

}
