package com.fx.crawler.application.service

import com.fx.common.adapter.out.persistence.repository.BatchRunRequestRepository
import com.fx.common.adapter.out.persistence.repository.BatchScheduleRepository
import com.fx.common.domain.batch.BatchJobNames
import com.fx.common.domain.batch.BatchRunRequest
import com.fx.common.domain.batch.BatchRunRequestStatus
import com.fx.crawler.application.port.`in`.BatchTriggerUseCase
import com.fx.crawler.support.CrawlerIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

/** 폴러가 부르는 흐름을 실제 스케줄 · 요청 테이블과 Job 실행으로 검증한다. */
class BatchTriggerIntegrationTest : CrawlerIntegrationTest() {

    @Autowired lateinit var batchTriggerUseCase: BatchTriggerUseCase
    @Autowired lateinit var batchScheduleRepository: BatchScheduleRepository
    @Autowired lateinit var batchRunRequestRepository: BatchRunRequestRepository

    @BeforeEach
    fun onlySilentPushSchedule() {
        jdbcTemplate.update("UPDATE batch_schedule SET enabled = (schedule_key = 'silent-push'), next_fire_at = NULL, last_fired_at = NULL")
    }

    @AfterEach
    fun restoreSchedules() {
        jdbcTemplate.update("UPDATE batch_schedule SET enabled = TRUE, next_fire_at = NULL, last_fired_at = NULL")
    }

    @Test
    fun `처음 본 스케줄은 발화 시각만 정하고, 발화 시각이 지나면 한 번만 실행한다`() {
        val before = silentPushExecutionCount()

        batchTriggerUseCase.triggerDueSchedules()

        val initialized = schedule()
        assertThat(initialized.nextFireAt).isAfter(LocalDateTime.now())
        assertThat(silentPushExecutionCount()).isEqualTo(before)

        val scheduledAt = LocalDateTime.now().minusMinutes(1).withNano(0)
        jdbcTemplate.update("UPDATE batch_schedule SET next_fire_at = ? WHERE schedule_key = 'silent-push'", scheduledAt)

        batchTriggerUseCase.triggerDueSchedules()
        batchTriggerUseCase.triggerDueSchedules()

        assertThat(silentPushExecutionCount()).isEqualTo(before + 1)
        val fired = schedule()
        assertThat(fired.nextFireAt).isAfter(LocalDateTime.now())
        assertThat(fired.lastFiredAt).isNotNull()

        val execution = jobRepository.getLastJobExecution(BatchJobNames.SILENT_PUSH, lastParameters())!!
        assertThat(execution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(execution.jobParameters.getLocalDateTime("scheduledAt")).isEqualTo(scheduledAt)
        assertThat(execution.jobParameters.getString("triggerType")).isEqualTo("SCHEDULED")
        assertThat(execution.jobParameters.getString("scheduleKey")).isEqualTo("silent-push")
    }

    @Test
    fun `수동 실행 요청을 실행하고, 실행할 수 없는 요청은 거절한다`() {
        val launched = batchRunRequestRepository.save(BatchRunRequest(BatchJobNames.SILENT_PUSH, "{}", "admin"))
        val unknown = batchRunRequestRepository.save(BatchRunRequest("unknownJob", "{}", "admin"))

        batchTriggerUseCase.processRunRequests()

        val launchedRequest = batchRunRequestRepository.findById(requireNotNull(launched.id)).orElseThrow()
        assertThat(launchedRequest.status).isEqualTo(BatchRunRequestStatus.LAUNCHED)
        val execution = jobRepository.getJobExecution(requireNotNull(launchedRequest.jobExecutionId))!!
        assertThat(execution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(execution.jobParameters.getLong("requestId")).isEqualTo(launched.id)
        assertThat(execution.jobParameters.getString("requestedBy")).isEqualTo("admin")

        val rejected = batchRunRequestRepository.findById(requireNotNull(unknown.id)).orElseThrow()
        assertThat(rejected.status).isEqualTo(BatchRunRequestStatus.REJECTED)
        assertThat(rejected.rejectReason).contains("unknownJob")
    }

    private fun schedule() =
        batchScheduleRepository.findAll().single {
            it.scheduleKey == "silent-push"
        }

    private fun silentPushExecutionCount(): Int =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM BATCH_JOB_EXECUTION e JOIN BATCH_JOB_INSTANCE i ON i.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID WHERE i.JOB_NAME = ?",
            Int::class.java,
            BatchJobNames.SILENT_PUSH,
        )!!

    private fun lastParameters() =
        jobRepository.getJobInstances(BatchJobNames.SILENT_PUSH, 0, 1).single().let { instance ->
            jobRepository.getLastJobExecution(instance)!!.jobParameters
        }

}
