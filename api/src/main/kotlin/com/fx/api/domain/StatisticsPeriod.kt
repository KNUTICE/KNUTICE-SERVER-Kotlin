package com.fx.api.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 통계 조회 기간. KST 날짜이고 양 끝을 포함한다.
 * @throws IllegalArgumentException 시작 날짜가 끝 날짜보다 늦거나 [MAX_DAYS] 일을 넘을 때
 */
data class StatisticsPeriod(
    val from: LocalDate,
    val to: LocalDate,
) {

    init {
        require(!from.isAfter(to)) {
            "조회 시작 날짜($from)가 끝 날짜($to)보다 늦습니다."
        }
        require(ChronoUnit.DAYS.between(from, to) < MAX_DAYS) {
            "조회 기간은 ${MAX_DAYS}일을 넘을 수 없습니다: $from ~ $to"
        }
    }

    companion object {

        const val MAX_DAYS = 366L
        const val DEFAULT_DAYS = 30L

        /** 끝 날짜가 없으면 가장 최근에 집계된 어제, 시작 날짜가 없으면 끝 날짜까지 [DEFAULT_DAYS] 일 */
        fun of(from: LocalDate?, to: LocalDate?, today: LocalDate): StatisticsPeriod {
            val end =
                to ?: today.minusDays(1)
            return StatisticsPeriod(from ?: end.minusDays(DEFAULT_DAYS - 1), end)
        }

    }

}

/** 기간 통계 조회 결과. [period] 는 기본값을 채운 실제 조회 기간이다. */
data class PeriodStatistics<T>(
    val period: StatisticsPeriod,
    val items: List<T>,
)
