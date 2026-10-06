package com.fx.api.domain

import com.fx.common.domain.batch.BatchTriggerType
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class BatchJobExecutionTest : BehaviorSpec({

    val startTime = LocalDateTime.of(2026, 10, 5, 16, 10, 1)

    fun execution(parameters: Map<String, String>, endTime: LocalDateTime? = startTime.plusSeconds(45)) =
        BatchJobExecution(1L, "noticeCrawlJob", parameters, "COMPLETED", "COMPLETED", null, startTime, endTime)

    Given("자동 실행 기록") {
        val scheduled = execution(
            mapOf(
                "topicType" to "MAJOR",
                "scheduledAt" to "2026-10-05T16:10",
                "triggerType" to "SCHEDULED",
                "scheduleKey" to "notice-crawl-major",
            )
        )

        Then("업무 파라미터만 남기고 실행 방식 · 스케줄 키를 따로 읽는다") {
            scheduled.parameters shouldBe mapOf("topicType" to "MAJOR")
            scheduled.triggerType shouldBe BatchTriggerType.SCHEDULED
            scheduled.scheduleKey shouldBe "notice-crawl-major"
            scheduled.requestedBy shouldBe null
            scheduled.durationMs shouldBe 45_000L
        }
    }

    Given("수동 실행 기록") {
        val manual = execution(mapOf("requestId" to "893497211373609235", "triggerType" to "MANUAL", "requestedBy" to "관리자"))

        Then("요청한 관리자를 읽는다") {
            manual.parameters shouldBe emptyMap()
            manual.triggerType shouldBe BatchTriggerType.MANUAL
            manual.requestedBy shouldBe "관리자"
            manual.scheduleKey shouldBe null
        }
    }

    Given("실행 중이거나 실행 방식이 남지 않은 기록") {
        val running = execution(emptyMap(), endTime = null)

        Then("실행 시간과 실행 방식이 없다") {
            running.durationMs shouldBe null
            running.triggerType shouldBe null
        }
    }

})
