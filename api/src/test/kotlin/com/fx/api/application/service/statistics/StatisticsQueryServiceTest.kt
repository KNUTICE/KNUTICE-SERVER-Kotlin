package com.fx.api.application.service.statistics

import com.fx.api.application.port.out.statistics.StatisticsPersistencePort
import com.fx.api.domain.DailyTopicStatistics
import com.fx.api.domain.DeviceCounts
import com.fx.api.domain.StatisticsPeriod
import com.fx.api.domain.TopicRequestCount
import com.fx.api.exception.StatisticsException
import com.fx.api.exception.errorcode.StatisticsErrorCode
import com.fx.api.fixture.TopicFixture
import com.fx.common.application.port.`in`.CatalogQueryUseCase
import com.fx.common.domain.TopicType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class StatisticsQueryServiceTest : BehaviorSpec({

    val zone = ZoneId.of("Asia/Seoul")
    val now = LocalDateTime.of(2026, 10, 13, 14, 7)
    val clock = Clock.fixed(now.atZone(zone).toInstant(), zone)

    val persistencePort = mockk<StatisticsPersistencePort>()
    val catalogQueryUseCase = mockk<CatalogQueryUseCase>()
    val service = StatisticsQueryService(persistencePort, catalogQueryUseCase, clock)

    every {
        catalogQueryUseCase.getTopicCatalog()
    } returns TopicFixture.CATALOG

    Given("기간을 주지 않으면") {
        val period = StatisticsPeriod(LocalDate.of(2026, 9, 13), LocalDate.of(2026, 10, 12))
        every {
            persistencePort.findDailyStatistics(period)
        } returns emptyList()

        When("일별 요약을 조회하면") {
            val result = service.getDailyStatistics(null, null)

            Then("어제까지 30일을 조회하고 그 기간을 돌려준다") {
                result.period shouldBe period
                verify {
                    persistencePort.findDailyStatistics(period)
                }
            }
        }
    }

    Given("기간이 잘못되면") {
        Then("STATISTICS_PERIOD_INVALID 예외에 이유를 담는다") {
            val reversed = shouldThrow<StatisticsException> {
                service.getEndpointStatistics(LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 1))
            }
            reversed.baseErrorCode shouldBe StatisticsErrorCode.STATISTICS_PERIOD_INVALID
            reversed.message shouldContain "늦습니다"

            val tooLong = shouldThrow<StatisticsException> {
                service.getDailyTokenStatistics(LocalDate.of(2025, 1, 1), LocalDate.of(2026, 10, 12))
            }
            tooLong.baseErrorCode shouldBe StatisticsErrorCode.STATISTICS_PERIOD_INVALID
            tooLong.message shouldContain "366일"
        }
    }

    Given("공지 · 학과 토픽 일부와 삭제된 토픽에 조회 기록이 있을 때") {
        val period = StatisticsPeriod(LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 12))
        every {
            persistencePort.findTopicRequestCounts(period)
        } returns listOf(
            TopicRequestCount(300, "COMPUTER_SOFTWARE", 5),
            TopicRequestCount(77, "DELETED_TOPIC", 9),
            TopicRequestCount(1, "GENERAL_NEWS", 5),
        )

        When("유형 없이 조회하면") {
            val topics = service.getTopicStatistics(null, period.from, period.to).items

            Then("조회 수 내림차순, 같으면 토픽 코드 순이고, 앱에 보이는 공지 · 학과 토픽은 조회가 없어도 0 으로 넣는다") {
                topics.map {
                    it.topicCode to it.requestCount
                } shouldBe listOf(77 to 9L, 1 to 5L, 300 to 5L, 2 to 0L)
            }

            Then("삭제된 토픽은 토픽 정보가 없고, 숨긴 토픽 · 학식 토픽은 조회가 없으면 넣지 않는다") {
                topics.first().topic shouldBe null
                topics.first().topicName shouldBe "DELETED_TOPIC"
                topics[1].topic shouldBe TopicFixture.GENERAL_NEWS
            }
        }

        When("유형을 주면") {
            Then("그 유형의 토픽만 담고 삭제된 토픽은 뺀다") {
                service.getTopicStatistics(TopicType.NOTICE, period.from, period.to).items.map {
                    it.topicCode
                } shouldBe listOf(1, 2)
                service.getTopicStatistics(TopicType.MAJOR, period.from, period.to).items.map {
                    it.topicCode
                } shouldBe listOf(300)
            }
        }
    }

    Given("토픽 하나의 일별 조회 수") {
        val period = StatisticsPeriod(LocalDate.of(2026, 10, 11), LocalDate.of(2026, 10, 12))
        val days = listOf(DailyTopicStatistics(period.from, 0), DailyTopicStatistics(period.to, 3))
        every {
            persistencePort.findDailyTopicStatistics(300, period)
        } returns days

        Then("영속성 포트의 결과를 그대로 담는다") {
            service.getDailyTopicStatistics(300, period.from, period.to).items shouldBe days
        }
    }

    Given("일부 토픽에만 구독자가 있을 때") {
        every {
            persistencePort.countSubscribersByTopic()
        } returns mapOf(1 to DeviceCounts(10, 6, 4), 900 to DeviceCounts(3, 2, 1))
        every {
            persistencePort.countActiveTokens()
        } returns DeviceCounts(20, 12, 8)

        When("구독자 수를 조회하면") {
            val subscriptions = service.getTopicSubscriptions()

            Then("카탈로그의 토픽 전체를 코드 순으로 담고 구독자가 없는 토픽은 0 이다") {
                subscriptions.measuredAt shouldBe now
                subscriptions.activeTokens shouldBe DeviceCounts(20, 12, 8)
                subscriptions.topics.map {
                    it.topic.code to it.subscribers
                } shouldBe listOf(
                    1 to DeviceCounts(10, 6, 4),
                    2 to DeviceCounts.ZERO,
                    5 to DeviceCounts.ZERO,
                    300 to DeviceCounts.ZERO,
                    900 to DeviceCounts(3, 2, 1),
                )
            }
        }
    }

})
