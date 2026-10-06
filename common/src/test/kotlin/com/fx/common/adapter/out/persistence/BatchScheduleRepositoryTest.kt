package com.fx.common.adapter.out.persistence

import com.fx.common.adapter.out.persistence.repository.BatchRunRequestRepository
import com.fx.common.adapter.out.persistence.repository.BatchScheduleRepository
import com.fx.common.domain.batch.BatchCron
import com.fx.common.domain.batch.BatchJob
import com.fx.common.domain.batch.BatchJobParameters
import com.fx.common.domain.batch.BatchRunRequest
import com.fx.common.domain.batch.BatchRunRequestStatus
import com.fx.persistence.MySqlContainerConfig
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.data.domain.Limit
import java.time.LocalDateTime

/** Flyway V6 의 스케줄 시드와 폴러가 쓰는 선점 쿼리를 실제 MySQL 에서 검증한다. */
@DataJpaTest(properties = ["spring.jpa.hibernate.ddl-auto=validate"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MySqlContainerConfig::class)
class BatchScheduleRepositoryTest @Autowired constructor(
    private val batchScheduleRepository: BatchScheduleRepository,
    private val batchRunRequestRepository: BatchRunRequestRepository,
    private val entityManager: EntityManager,
) {

    private val now = LocalDateTime.of(2026, 9, 30, 10, 7)

    @Test
    fun `레거시 cron 과 요약 스케줄이 시드된다`() {
        val schedules = batchScheduleRepository.findAll().associateBy {
            it.scheduleKey
        }

        assertThat(schedules.mapValues { (_, it) ->
            it.jobName to it.cron
        }).containsExactlyInAnyOrderEntriesOf(
            mapOf(
                "notice-crawl-notice" to (BatchJob.NOTICE_CRAWL.jobName to "0 0/15 * * * *"),
                "notice-crawl-major" to (BatchJob.NOTICE_CRAWL.jobName to "0 10 16 * * *"),
                "notice-summary" to (BatchJob.NOTICE_SUMMARY.jobName to "0 */5 * * * *"),
                "meal-notify" to (BatchJob.MEAL_NOTIFY.jobName to "0 10 10 * * MON-FRI"),
                "silent-push" to (BatchJob.SILENT_PUSH.jobName to "0 0 0 1 * *"),
                "seat-alert-check" to (BatchJob.SEAT_ALERT_CHECK.jobName to "0 * * * * *"),
                "batch-maintenance" to (BatchJob.MAINTENANCE.jobName to "0 30 4 * * *"),
            )
        )
        schedules.values.forEach { schedule ->
            assertThatCode {
                BatchCron.parse(schedule.cron)
            }.doesNotThrowAnyException()
            assertThatCode {
                BatchJob.from(schedule.jobName)!!.validate(BatchJobParameters.parse(schedule.jobParameters))
            }.doesNotThrowAnyException()
            assertThat(schedule.nextFireAt).isNull()
        }
        assertThat(schedules.values).allMatch {
            it.enabled
        }
        assertThat(BatchJobParameters.parse(schedules.getValue("batch-maintenance").jobParameters))
            .isEqualTo(mapOf("retentionDays" to "7"))
    }

    @Test
    fun `다음 발화 시각은 비어 있을 때 한 번만 채운다`() {
        val schedule = batchScheduleRepository.findAllByEnabledTrueAndNextFireAtIsNull().first()
        val id = requireNotNull(schedule.id)

        assertThat(batchScheduleRepository.initializeNextFireAt(id, now.plusMinutes(8), now)).isEqualTo(1)
        assertThat(batchScheduleRepository.initializeNextFireAt(id, now.plusMinutes(20), now)).isEqualTo(0)
    }

    @Test
    fun `발화는 읽은 발화 시각이 그대로일 때 한 번만 선점된다`() {
        val schedule = batchScheduleRepository.findAll().first {
            it.scheduleKey == "notice-crawl-notice"
        }
        val id = requireNotNull(schedule.id)
        val previous = LocalDateTime.of(2026, 9, 30, 10, 0)
        batchScheduleRepository.initializeNextFireAt(id, previous, now)

        assertThat(batchScheduleRepository.findAllByEnabledTrueAndNextFireAtLessThanEqualOrderByNextFireAtAsc(now).map {
            it.id
        })
            .contains(id)
        assertThat(batchScheduleRepository.claim(id, previous, now.plusMinutes(8), now)).isEqualTo(1)
        assertThat(batchScheduleRepository.claim(id, previous, now.plusMinutes(8), now)).isEqualTo(0)

        entityManager.clear()
        val claimed = batchScheduleRepository.findById(id).orElseThrow()
        assertThat(claimed.nextFireAt).isEqualTo(now.plusMinutes(8))
        assertThat(claimed.lastFiredAt).isEqualTo(now)
    }

    @Test
    fun `수동 실행 요청은 한 번만 선점되고 결과가 기록된다`() {
        val launched = batchRunRequestRepository.saveAndFlush(BatchRunRequest(BatchJob.SILENT_PUSH.jobName, "{}", "admin"))
        val rejected = batchRunRequestRepository.saveAndFlush(BatchRunRequest("unknownJob", "{}", "admin"))
        val launchedId = requireNotNull(launched.id)
        val rejectedId = requireNotNull(rejected.id)

        assertThat(batchRunRequestRepository.findAllByStatusOrderByIdAsc(BatchRunRequestStatus.REQUESTED, Limit.of(10)).map {
            it.id
        })
            .containsExactly(launchedId, rejectedId)
        assertThat(batchRunRequestRepository.claim(launchedId, now)).isEqualTo(1)
        assertThat(batchRunRequestRepository.claim(launchedId, now)).isEqualTo(0)
        batchRunRequestRepository.updateJobExecutionId(launchedId, 42L, now)
        batchRunRequestRepository.reject(rejectedId, "등록되지 않은 Job 입니다", now)

        entityManager.clear()
        val launchedRequest = batchRunRequestRepository.findById(launchedId).orElseThrow()
        assertThat(launchedRequest.status).isEqualTo(BatchRunRequestStatus.LAUNCHED)
        assertThat(launchedRequest.jobExecutionId).isEqualTo(42L)
        assertThat(launchedRequest.processedAt).isEqualTo(now)
        val rejectedRequest = batchRunRequestRepository.findById(rejectedId).orElseThrow()
        assertThat(rejectedRequest.status).isEqualTo(BatchRunRequestStatus.REJECTED)
        assertThat(rejectedRequest.rejectReason).isEqualTo("등록되지 않은 Job 입니다")
    }

}
