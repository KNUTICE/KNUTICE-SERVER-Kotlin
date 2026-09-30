package com.fx.crawler.adapter.`in`.batch.job

import com.fx.crawler.support.CrawlerIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.job.Job
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class MaintenanceJobTest : CrawlerIntegrationTest() {

    @Autowired @Qualifier("maintenanceJob") lateinit var maintenanceJob: Job
    @Autowired @Qualifier("silentPushJob") lateinit var silentPushJob: Job

    @Test
    fun `보존 기간이 지난 실행 기록만 딸린 행과 함께 지운다`() {
        val old = run(silentPushJob)
        val recent = run(silentPushJob)
        jdbcTemplate.update(
            "UPDATE BATCH_JOB_EXECUTION SET END_TIME = DATE_SUB(NOW(6), INTERVAL 8 DAY) WHERE JOB_EXECUTION_ID = ?",
            old.id,
        )

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
    fun `보존 일수가 없으면 실패한다`() {
        assertThat(run(maintenanceJob).status).isEqualTo(BatchStatus.FAILED)
    }

    private fun countExecution(jobExecutionId: Long) =
        count("SELECT COUNT(*) FROM BATCH_JOB_EXECUTION WHERE JOB_EXECUTION_ID = ?", jobExecutionId)

    private fun count(sql: String, id: Long): Int =
        jdbcTemplate.queryForObject(sql, Int::class.java, id)!!

}
