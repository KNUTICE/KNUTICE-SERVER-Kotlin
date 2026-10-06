package com.fx.api.application.service.dashboard

import com.fx.api.application.port.out.batch.BatchExecutionPersistencePort
import com.fx.api.application.port.out.batch.BatchSchedulePersistencePort
import com.fx.api.application.port.out.notice.NoticePersistencePort
import com.fx.api.domain.BatchExecutionCounts
import com.fx.api.domain.BatchJobExecution
import com.fx.api.domain.LastJobExecution
import com.fx.common.domain.batch.BatchSchedule
import com.fx.persistence.withId
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

class DashboardQueryServiceTest : BehaviorSpec({

    val zone = ZoneId.of("Asia/Seoul")
    val now = LocalDateTime.of(2026, 10, 6, 14, 7)
    val clock = Clock.fixed(now.atZone(zone).toInstant(), zone)

    val schedulePort = mockk<BatchSchedulePersistencePort>()
    val executionPort = mockk<BatchExecutionPersistencePort>()
    val noticePort = mockk<NoticePersistencePort>()
    val service = DashboardQueryService(schedulePort, executionPort, noticePort, clock)

    fun schedule(id: Long, scheduleKey: String, cron: String, enabled: Boolean = true) =
        BatchSchedule(scheduleKey, "silentPushJob", "{}", cron, false, scheduleKey)
            .withId(id)
            .apply {
                if (enabled) {
                    enable(now)
                }
            }

    Given("켜진 스케줄 · 꺼진 스케줄 · 아직 다음 실행 시각이 없는 스케줄이 있을 때") {
        val daily = schedule(1L, "daily", "0 0 4 * * *")
        val minutely = schedule(2L, "minutely", "0 * * * * *")
        val disabled = schedule(3L, "disabled", "0 0 0 1 * *", enabled = false)
        // 생성자로 켜고 폴러가 아직 다음 실행 시각을 채우지 않은 스케줄
        val uninitialized = BatchSchedule("uninitialized", "silentPushJob", "{}", "0 0 12 * * *", true, "새 스케줄").withId(4L)
        every {
            schedulePort.findAll()
        } returns listOf(daily, disabled, minutely, uninitialized)
        every {
            executionPort.findLastExecutions(listOf("minutely", "daily", "uninitialized"))
        } returns mapOf("minutely" to LastJobExecution(48290L, "COMPLETED"))
        every {
            executionPort.countFailedAndRunning(now.minusHours(24))
        } returns BatchExecutionCounts(failed = 1, running = 2)
        every {
            noticePort.countPendingSummaries()
        } returns 4
        val recent = BatchJobExecution(48290L, "silentPushJob", emptyMap(), "COMPLETED", "COMPLETED", null, now, now)
        every {
            executionPort.findExecutions(null, null, PageRequest.of(0, 8))
        } returns PageImpl(listOf(recent))

        When("대시보드를 조회하면") {
            val dashboard = service.getDashboard()

            Then("24시간 안의 실패 수 · 실행 중인 수 · 요약 대기 수 · 꺼진 스케줄 수를 담는다") {
                dashboard.failedLast24h shouldBe 1
                dashboard.running shouldBe 2
                dashboard.pendingSummaries shouldBe 4
                dashboard.disabledSchedules shouldBe 1
            }

            Then("켜진 스케줄만 다음 실행이 빠른 순으로 담고, 다음 실행 시각이 없는 스케줄은 맨 뒤에 둔다") {
                dashboard.upcoming.map {
                    it.schedule.scheduleKey
                } shouldBe listOf("minutely", "daily", "uninitialized")
                dashboard.upcoming[0].lastExecution shouldBe LastJobExecution(48290L, "COMPLETED")
                dashboard.upcoming[1].lastExecution shouldBe null
            }

            Then("최근 실행 8건을 담는다") {
                dashboard.recentExecutions shouldBe listOf(recent)
            }
        }
    }

})
