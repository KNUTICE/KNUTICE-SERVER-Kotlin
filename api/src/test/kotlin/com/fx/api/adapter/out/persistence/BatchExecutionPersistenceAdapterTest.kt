package com.fx.api.adapter.out.persistence

import com.fx.api.domain.BatchJobExecution
import com.fx.api.domain.BatchStepExecution
import com.fx.api.domain.LastJobExecution
import com.fx.api.exception.BatchException
import com.fx.api.exception.errorcode.BatchErrorCode
import com.fx.common.domain.batch.BatchTriggerType
import com.fx.persistence.MySqlContainerConfig
import com.fx.persistence.request.PagingRequest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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

    /**
     * 오래된 순으로
     * - 10 : 학과 크롤링 자동 실행, 성공
     * - 11 : 학과 크롤링 자동 실행, 실패 (Step 2개)
     * - 12 : 학과 크롤링 수동 실행, 성공
     * - 13 : 사일런트 푸시 자동 실행, 실행 중
     */
    @BeforeEach
    fun setUp() {
        jdbcTemplate.update("INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY) VALUES (1, 0, 'noticeCrawlJob', 'key-1')")
        jdbcTemplate.update("INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY) VALUES (2, 0, 'silentPushJob', 'key-2')")

        scheduledExecution(10, jobInstanceId = 1, scheduleKey = "notice-crawl-major", status = "COMPLETED")
        parameter(10, "topicType", "MAJOR", identifying = true)
        scheduledExecution(11, jobInstanceId = 1, scheduleKey = "notice-crawl-major", status = "FAILED", exitMessage = "java.net.SocketTimeoutException")
        parameter(11, "topicType", "MAJOR", identifying = true)
        execution(12, jobInstanceId = 1, status = "COMPLETED")
        parameter(12, "topicType", "NOTICE", identifying = true)
        parameter(12, "requestId", "893497211373609235", identifying = true)
        parameter(12, "triggerType", "MANUAL")
        parameter(12, "requestedBy", "관리자")
        scheduledExecution(13, jobInstanceId = 2, scheduleKey = "silent-push", status = "STARTED", running = true)

        step(101, jobExecutionId = 11, stepName = "noticeCrawlStep", status = "COMPLETED", writeCount = 3)
        step(102, jobExecutionId = 11, stepName = "noticePushStep", status = "FAILED", writeCount = 0, exitMessage = "FCM 오류")
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
        assertThat(batchExecutionPersistenceAdapter.findLastExecutions(emptyList())).isEmpty()
    }

    @Test
    fun `실행을 최신순으로 페이지 단위 조회한다`() {
        val first = batchExecutionPersistenceAdapter.findExecutions(null, null, PagingRequest(page = 1, size = 3).toPageable())
        assertThat(idsOf(first.content)).containsExactly(13L, 12L, 11L)
        assertThat(first.totalElements).isEqualTo(4)
        assertThat(first.hasNext()).isTrue()

        val second = batchExecutionPersistenceAdapter.findExecutions(null, null, PagingRequest(page = 2, size = 3).toPageable())
        assertThat(idsOf(second.content)).containsExactly(10L)
    }

    @Test
    fun `Job 이름과 상태로 거를 수 있다`() {
        val pageable = PagingRequest().toPageable()

        assertThat(idsOf(batchExecutionPersistenceAdapter.findExecutions("noticeCrawlJob", null, pageable).content)).containsExactly(12L, 11L, 10L)
        assertThat(idsOf(batchExecutionPersistenceAdapter.findExecutions(null, "FAILED", pageable).content)).containsExactly(11L)
        assertThat(idsOf(batchExecutionPersistenceAdapter.findExecutions("noticeCrawlJob", "STARTED", pageable).content)).isEmpty()
    }

    @Test
    fun `실행마다 파라미터를 붙인다`() {
        val executions = batchExecutionPersistenceAdapter.findExecutions(null, null, PagingRequest().toPageable()).content.associateBy {
            it.id
        }

        val manual = executions.getValue(12L)
        assertThat(manual.parameters).isEqualTo(mapOf("topicType" to "NOTICE"))
        assertThat(manual.triggerType).isEqualTo(BatchTriggerType.MANUAL)
        assertThat(manual.requestedBy).isEqualTo("관리자")

        val running = executions.getValue(13L)
        assertThat(running.scheduleKey).isEqualTo("silent-push")
        assertThat(running.endTime).isNull()
    }

    @Test
    fun `실행 하나와 그 Step 을 실행 순서로 조회한다`() {
        val execution = batchExecutionPersistenceAdapter.getExecution(11L)
        assertThat(execution.status).isEqualTo("FAILED")
        assertThat(execution.exitMessage).isEqualTo("java.net.SocketTimeoutException")
        assertThat(execution.startTime).isEqualTo(startTime)
        assertThat(execution.durationMs).isEqualTo(30_000L)

        val steps = batchExecutionPersistenceAdapter.findSteps(11L)
        assertThat(steps.map(BatchStepExecution::stepName)).containsExactly("noticeCrawlStep", "noticePushStep")
        assertThat(steps[0].writeCount).isEqualTo(3)
        assertThat(steps[1].skipCount).isEqualTo(6)
        assertThat(steps[1].exitMessage).isEqualTo("FCM 오류")
    }

    @Test
    fun `없는 실행은 EXECUTION_NOT_FOUND 예외가 발생한다`() {
        assertThatThrownBy {
            batchExecutionPersistenceAdapter.getExecution(999L)
        }.isInstanceOfSatisfying(BatchException::class.java) {
            assertThat(it.baseErrorCode).isEqualTo(BatchErrorCode.EXECUTION_NOT_FOUND)
        }
        assertThat(batchExecutionPersistenceAdapter.findSteps(999L)).isEmpty()
    }

    private fun idsOf(executions: List<BatchJobExecution>): List<Long> =
        executions.map {
            it.id
        }

    private fun scheduledExecution(
        id: Long,
        jobInstanceId: Long,
        scheduleKey: String,
        status: String,
        exitMessage: String? = null,
        running: Boolean = false,
    ) {
        execution(id, jobInstanceId, status, exitMessage, running)
        parameter(id, "scheduledAt", "2026-10-06T16:10", identifying = true)
        parameter(id, "triggerType", "SCHEDULED")
        parameter(id, "scheduleKey", scheduleKey)
    }

    private fun execution(id: Long, jobInstanceId: Long, status: String, exitMessage: String? = null, running: Boolean = false) {
        jdbcTemplate.update(
            """
            INSERT INTO BATCH_JOB_EXECUTION (JOB_EXECUTION_ID, VERSION, JOB_INSTANCE_ID, CREATE_TIME, START_TIME, END_TIME, STATUS, EXIT_CODE, EXIT_MESSAGE)
            VALUES (?, 0, ?, ?, ?, ?, ?, ?, ?)
            """,
            id, jobInstanceId, startTime, startTime, if (running) null else startTime.plusSeconds(30), status, if (running) "UNKNOWN" else status, exitMessage,
        )
    }

    private fun parameter(jobExecutionId: Long, name: String, value: String, identifying: Boolean = false) {
        jdbcTemplate.update(
            "INSERT INTO BATCH_JOB_EXECUTION_PARAMS (JOB_EXECUTION_ID, PARAMETER_NAME, PARAMETER_TYPE, PARAMETER_VALUE, IDENTIFYING) VALUES (?, ?, 'java.lang.String', ?, ?)",
            jobExecutionId, name, value, if (identifying) "Y" else "N",
        )
    }

    /** 건너뛴 건수는 읽기 1 · 처리 2 · 쓰기 3 */
    private fun step(id: Long, jobExecutionId: Long, stepName: String, status: String, writeCount: Long, exitMessage: String? = null) {
        jdbcTemplate.update(
            """
            INSERT INTO BATCH_STEP_EXECUTION (STEP_EXECUTION_ID, VERSION, STEP_NAME, JOB_EXECUTION_ID, CREATE_TIME, START_TIME, END_TIME, STATUS,
                COMMIT_COUNT, READ_COUNT, FILTER_COUNT, WRITE_COUNT, READ_SKIP_COUNT, WRITE_SKIP_COUNT, PROCESS_SKIP_COUNT, ROLLBACK_COUNT, EXIT_CODE, EXIT_MESSAGE)
            VALUES (?, 0, ?, ?, ?, ?, ?, ?, 1, ?, 0, ?, 1, 3, 2, 0, ?, ?)
            """,
            id, stepName, jobExecutionId, startTime, startTime, startTime.plusSeconds(10), status, writeCount, writeCount, status, exitMessage,
        )
    }

}
