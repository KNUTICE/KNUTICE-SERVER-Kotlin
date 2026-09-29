package com.fx.crawler.application.service

import com.fx.common.application.port.out.WebhookPort
import com.fx.common.domain.batch.BatchJobNames
import com.fx.common.domain.batch.BatchRunRequest
import com.fx.common.domain.batch.BatchSchedule
import com.fx.crawler.application.port.out.BatchRunRequestPersistencePort
import com.fx.crawler.application.port.out.BatchSchedulePersistencePort
import com.fx.crawler.application.port.out.JobLaunchPort
import com.fx.crawler.config.CrawlerProperties
import com.fx.crawler.domain.batch.JobLaunchRequest
import com.fx.crawler.domain.batch.JobTrigger
import com.fx.persistence.withId
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

class BatchTriggerServiceTest {

    private val zone = ZoneId.of("Asia/Seoul")
    private val now = LocalDateTime.of(2026, 9, 30, 10, 7)
    private val clock = Clock.fixed(now.atZone(zone).toInstant(), zone)

    private val schedulePort = mockk<BatchSchedulePersistencePort>(relaxed = true)
    private val requestPort = mockk<BatchRunRequestPersistencePort>(relaxed = true)
    private val jobLaunchPort = mockk<JobLaunchPort>()
    private val webhookPort = mockk<WebhookPort>(relaxed = true)
    private val service = BatchTriggerService(schedulePort, requestPort, jobLaunchPort, webhookPort, CrawlerProperties(), clock)

    @BeforeEach
    fun setUp() {
        every { schedulePort.findUninitialized() } returns emptyList()
        every { schedulePort.findDue(any()) } returns emptyList()
        every { jobLaunchPort.exists(any()) } returns true
        every { jobLaunchPort.isRunning(any(), any()) } returns false
        every { jobLaunchPort.launch(any()) } returns 100L
    }

    /** 10:00 에 발화하도록 켠 스케줄 (지금은 10:07) */
    private fun dueSchedule(jobParameters: String = """{"topicType":"NOTICE"}""", jobName: String = BatchJobNames.NOTICE_CRAWL) =
        BatchSchedule("notice-crawl-notice", jobName, jobParameters, "0 0/15 * * * *", false, "공지")
            .withId(1L)
            .apply { enable(LocalDateTime.of(2026, 9, 30, 9, 50)) }

    @Test
    fun `발화 시각이 없는 스케줄은 다음 발화 시각만 채우고 실행하지 않는다`() {
        val schedule = BatchSchedule("silent-push", BatchJobNames.SILENT_PUSH, "{}", "0 0/15 * * * *", true, "사일런트").withId(2L)
        every { schedulePort.findUninitialized() } returns listOf(schedule)

        service.triggerDueSchedules()

        verify { schedulePort.initializeNextFireAt(2L, LocalDateTime.of(2026, 9, 30, 10, 15), now) }
        verify(exactly = 0) { jobLaunchPort.launch(any()) }
    }

    @Test
    fun `발화를 선점하면 발화 시각을 파라미터로 실행하고 다음 발화 시각은 지금 이후로 넘긴다`() {
        every { schedulePort.findDue(now) } returns listOf(dueSchedule())
        every { schedulePort.claim(any(), any(), any(), any()) } returns true
        val launched = slot<JobLaunchRequest>()
        every { jobLaunchPort.launch(capture(launched)) } returns 100L

        service.triggerDueSchedules()

        verify { schedulePort.claim(1L, LocalDateTime.of(2026, 9, 30, 10, 0), LocalDateTime.of(2026, 9, 30, 10, 15), now) }
        assertThat(launched.captured).isEqualTo(
            JobLaunchRequest(
                jobName = BatchJobNames.NOTICE_CRAWL,
                parameters = mapOf("topicType" to "NOTICE"),
                trigger = JobTrigger.Scheduled("notice-crawl-notice", LocalDateTime.of(2026, 9, 30, 10, 0)),
            )
        )
    }

    @Test
    fun `다른 인스턴스가 먼저 선점한 발화는 실행하지 않는다`() {
        every { schedulePort.findDue(now) } returns listOf(dueSchedule())
        every { schedulePort.claim(any(), any(), any(), any()) } returns false

        service.triggerDueSchedules()

        verify(exactly = 0) { jobLaunchPort.launch(any()) }
    }

    @Test
    fun `같은 작업이 실행 중이면 이번 발화는 건너뛰고 알리지 않는다`() {
        every { schedulePort.findDue(now) } returns listOf(dueSchedule())
        every { schedulePort.claim(any(), any(), any(), any()) } returns true
        every { jobLaunchPort.isRunning(BatchJobNames.NOTICE_CRAWL, mapOf("topicType" to "NOTICE")) } returns true

        service.triggerDueSchedules()

        verify(exactly = 0) { jobLaunchPort.launch(any()) }
        verify(exactly = 0) { webhookPort.notifySlack(any()) }
    }

    @Test
    fun `모르는 Job 이나 잘못된 파라미터의 스케줄은 실행하지 않고 Slack 으로 알린다`() {
        every { schedulePort.findDue(now) } returns listOf(dueSchedule(jobName = "unknownJob"), dueSchedule(jobParameters = """{"topicType":1}"""))
        every { schedulePort.claim(any(), any(), any(), any()) } returns true
        every { jobLaunchPort.exists("unknownJob") } returns false

        service.triggerDueSchedules()

        verify(exactly = 0) { jobLaunchPort.launch(any()) }
        verify(exactly = 2) { webhookPort.notifySlack(any()) }
    }

    @Test
    fun `수동 요청을 선점해 실행하고 JobExecution 을 기록한다`() {
        val request = BatchRunRequest(BatchJobNames.SILENT_PUSH, "{}", "admin").withId(7L)
        every { requestPort.findRequested(any()) } returns listOf(request)
        every { requestPort.claim(7L, any()) } returns true
        val launched = slot<JobLaunchRequest>()
        every { jobLaunchPort.launch(capture(launched)) } returns 100L

        service.processRunRequests()

        assertThat(launched.captured.trigger).isEqualTo(JobTrigger.Manual(7L, "admin"))
        verify { requestPort.recordJobExecution(7L, 100L, now) }
    }

    @Test
    fun `실행할 수 없는 수동 요청은 사유와 함께 거절한다`() {
        val unknown = BatchRunRequest("unknownJob", "{}", "admin").withId(7L)
        val running = BatchRunRequest(BatchJobNames.SILENT_PUSH, "{}", "admin").withId(8L)
        val invalid = BatchRunRequest(BatchJobNames.SILENT_PUSH, "not-json", "admin").withId(9L)
        every { requestPort.findRequested(any()) } returns listOf(unknown, running, invalid)
        every { requestPort.claim(any(), any()) } returns true
        every { jobLaunchPort.exists("unknownJob") } returns false
        every { jobLaunchPort.isRunning(BatchJobNames.SILENT_PUSH, emptyMap()) } returns true

        service.processRunRequests()

        verify { requestPort.reject(7L, "등록되지 않은 Job 입니다: unknownJob", now) }
        verify { requestPort.reject(8L, BatchTriggerService.ALREADY_RUNNING, now) }
        verify { requestPort.reject(9L, match { it.contains("JSON") }, now) }
        verify(exactly = 0) { jobLaunchPort.launch(any()) }
    }

    @Test
    fun `다른 인스턴스가 가져간 수동 요청은 건너뛴다`() {
        every { requestPort.findRequested(any()) } returns listOf(BatchRunRequest(BatchJobNames.SILENT_PUSH, "{}", "admin").withId(7L))
        every { requestPort.claim(7L, any()) } returns false

        service.processRunRequests()

        verify(exactly = 0) { jobLaunchPort.launch(any()) }
        verify(exactly = 0) { requestPort.reject(any(), any(), any()) }
    }

    @Test
    fun `실행 중에 멈춘 Job 을 정리하면 Slack 으로 알린다`() {
        every { jobLaunchPort.recoverInterrupted() } returns listOf(10L, 11L)

        service.recoverInterruptedJobs()

        verify(exactly = 1) { webhookPort.notifySlack(any()) }
    }

}
