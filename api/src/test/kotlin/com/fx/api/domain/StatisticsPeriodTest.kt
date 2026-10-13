package com.fx.api.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class StatisticsPeriodTest : BehaviorSpec({

    val today = LocalDate.of(2026, 10, 13)

    Given("날짜를 주지 않으면") {
        Then("어제까지 30일이다") {
            StatisticsPeriod.of(null, null, today) shouldBe
                StatisticsPeriod(LocalDate.of(2026, 9, 13), LocalDate.of(2026, 10, 12))
        }
    }

    Given("한쪽 날짜만 주면") {
        Then("끝 날짜만 있으면 그날까지 30일, 시작 날짜만 있으면 어제까지다") {
            StatisticsPeriod.of(null, LocalDate.of(2026, 10, 5), today) shouldBe
                StatisticsPeriod(LocalDate.of(2026, 9, 6), LocalDate.of(2026, 10, 5))
            StatisticsPeriod.of(LocalDate.of(2026, 10, 1), null, today) shouldBe
                StatisticsPeriod(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 12))
        }
    }

    Given("기간 길이") {
        Then("하루 · 366일은 허용한다") {
            StatisticsPeriod(today, today).from shouldBe today
            StatisticsPeriod(LocalDate.of(2025, 10, 12), LocalDate.of(2026, 10, 12)).to shouldBe LocalDate.of(2026, 10, 12)
        }

        Then("367일이거나 시작 날짜가 끝 날짜보다 늦으면 거절한다") {
            shouldThrow<IllegalArgumentException> {
                StatisticsPeriod(LocalDate.of(2025, 10, 11), LocalDate.of(2026, 10, 12))
            }
            shouldThrow<IllegalArgumentException> {
                StatisticsPeriod.of(today, null, today)
            }
        }
    }

    Given("요청 수 · 처리 시간 합계") {
        fun counts(requestCount: Long, totalDurationMs: Long) =
            RequestCounts(requestCount, 0, 0, totalDurationMs, null)

        Then("평균 처리 시간은 합계 ÷ 요청 수를 소수 첫째 자리에서 반올림한다") {
            counts(150, 1250).averageDurationMs shouldBe 8.3
            counts(100, 25).averageDurationMs shouldBe 0.3
            counts(4, 10).averageDurationMs shouldBe 2.5
        }

        Then("요청이 없으면 평균이 없다") {
            counts(0, 0).averageDurationMs shouldBe null
        }
    }

})
