package com.fx.crawler.adapter.`in`.batch.job

import com.fx.crawler.support.CrawlerIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class MaintenanceJobTest : CrawlerIntegrationTest() {

    @Autowired @Qualifier("maintenanceJob") lateinit var maintenanceJob: Job
    @Autowired @Qualifier("silentPushJob") lateinit var silentPushJob: Job

    @BeforeEach
    fun expirePreviousExecutions() {
        // 다른 테스트가 남긴 실행 기록은 모두 보존 기간이 지난 것으로 둬서, 이 테스트의 실행만 남게 한다
        expireExecutions()
    }

    @Test
    fun `보존 기간이 지난 실행 기록만 딸린 행과 함께 지운다`() {
        val old = run(silentPushJob)
        expireExecutions()
        val recent = run(silentPushJob)

        val execution = run(maintenanceJob, mapOf("retentionDays" to "7"))

        assertThat(execution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(countExecution(old.id)).isZero()
        assertThat(count("SELECT COUNT(*) FROM BATCH_STEP_EXECUTION WHERE JOB_EXECUTION_ID = ?", old.id)).isZero()
        assertThat(count("SELECT COUNT(*) FROM BATCH_JOB_EXECUTION_PARAMS WHERE JOB_EXECUTION_ID = ?", old.id)).isZero()
        assertThat(count("SELECT COUNT(*) FROM BATCH_JOB_INSTANCE WHERE JOB_INSTANCE_ID = ?", old.jobInstanceId)).isZero()
        assertThat(countExecution(recent.id)).isEqualTo(1)
        assertThat(countExecution(execution.id)).isEqualTo(1)
    }

    @Test
    fun `실행 기록을 만들기 직전의 새 JobInstance 는 지우지 않는다`() {
        val old = run(silentPushJob)
        expireExecutions()
        run(silentPushJob)
        // Job 시작 중 JobInstance 만 커밋되고 JobExecution 은 아직 없는 순간
        val starting = jobRepository.createJobInstance("silentPushJob", JobParametersBuilder().addString("run", "starting").toJobParameters())

        run(maintenanceJob, mapOf("retentionDays" to "7"))

        assertThat(count("SELECT COUNT(*) FROM BATCH_JOB_INSTANCE WHERE JOB_INSTANCE_ID = ?", old.jobInstanceId)).isZero()
        assertThat(count("SELECT COUNT(*) FROM BATCH_JOB_INSTANCE WHERE JOB_INSTANCE_ID = ?", starting.id)).isEqualTo(1)
    }

    @Test
    fun `보존 일수가 없으면 실패한다`() {
        assertThat(run(maintenanceJob).status).isEqualTo(BatchStatus.FAILED)
    }

    private fun expireExecutions() {
        jdbcTemplate.update("UPDATE BATCH_JOB_EXECUTION SET END_TIME = DATE_SUB(NOW(6), INTERVAL 8 DAY) WHERE END_TIME IS NOT NULL")
    }

    private fun countExecution(jobExecutionId: Long) =
        count("SELECT COUNT(*) FROM BATCH_JOB_EXECUTION WHERE JOB_EXECUTION_ID = ?", jobExecutionId)

    private fun count(sql: String, id: Long): Int =
        jdbcTemplate.queryForObject(sql, Int::class.java, id)!!

}
