package com.fx.api.adapter.out.persistence

import com.fx.api.domain.LastJobExecution
import com.fx.persistence.MySqlContainerConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDateTime

/** crawler 가 남기는 형태로 `BATCH_*` 행을 넣고 실제 MySQL 에서 조회한다. */
@DataJpaTest(properties = ["MYSQL_URL=unused", "MYSQL_DATABASE=unused", "MYSQL_USERNAME=unused", "MYSQL_PASSWORD=unused"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MySqlContainerConfig::class, BatchExecutionPersistenceAdapter::class)
class BatchExecutionPersistenceAdapterTest @Autowired constructor(
    private val batchExecutionPersistenceAdapter: BatchExecutionPersistenceAdapter,
    private val jdbcTemplate: JdbcTemplate,
) {

    private val startTime = LocalDateTime.of(2026, 10, 6, 16, 10, 1)

    @BeforeEach
    fun setUp() {
        jdbcTemplate.update("INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY) VALUES (1, 0, 'noticeCrawlJob', 'key-1')")
        jdbcTemplate.update("INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY) VALUES (2, 0, 'silentPushJob', 'key-2')")

        scheduledExecution(10, jobInstanceId = 1, scheduleKey = "notice-crawl-major", status = "COMPLETED")
        scheduledExecution(11, jobInstanceId = 1, scheduleKey = "notice-crawl-major", status = "FAILED")
        scheduledExecution(13, jobInstanceId = 2, scheduleKey = "silent-push", status = "STARTED")
        // 수동 실행은 scheduleKey 가 없으므로 스케줄의 실행이 아니다
        execution(12, jobInstanceId = 1, status = "COMPLETED")
        parameter(12, "triggerType", "MANUAL")
        parameter(12, "requestedBy", "admin")
    }

    @Test
    fun `스케줄 키별로 마지막으로 시작한 실행을 찾는다`() {
        val lastExecutions = batchExecutionPersistenceAdapter.findLastExecutions(
            listOf("notice-crawl-major", "silent-push", "meal-notify")
        )

        assertThat(lastExecutions).containsExactlyInAnyOrderEntriesOf(
            mapOf(
                "notice-crawl-major" to LastJobExecution(11, "FAILED"),
                "silent-push" to LastJobExecution(13, "STARTED"),
            )
        )
    }

    @Test
    fun `스케줄 키가 없으면 조회하지 않는다`() {
        assertThat(batchExecutionPersistenceAdapter.findLastExecutions(emptyList())).isEmpty()
    }

    private fun scheduledExecution(id: Long, jobInstanceId: Long, scheduleKey: String, status: String) {
        execution(id, jobInstanceId, status)
        parameter(id, "triggerType", "SCHEDULED")
        parameter(id, "scheduleKey", scheduleKey)
    }

    private fun execution(id: Long, jobInstanceId: Long, status: String) {
        jdbcTemplate.update(
            "INSERT INTO BATCH_JOB_EXECUTION (JOB_EXECUTION_ID, VERSION, JOB_INSTANCE_ID, CREATE_TIME, START_TIME, STATUS) VALUES (?, 0, ?, ?, ?, ?)",
            id, jobInstanceId, startTime, startTime, status,
        )
    }

    private fun parameter(jobExecutionId: Long, name: String, value: String) {
        jdbcTemplate.update(
            "INSERT INTO BATCH_JOB_EXECUTION_PARAMS (JOB_EXECUTION_ID, PARAMETER_NAME, PARAMETER_TYPE, PARAMETER_VALUE, IDENTIFYING) VALUES (?, ?, 'java.lang.String', ?, 'N')",
            jobExecutionId, name, value,
        )
    }

}
