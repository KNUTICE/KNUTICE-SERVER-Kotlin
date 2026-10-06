package com.fx.api.adapter.out.persistence

import com.fx.api.application.port.out.batch.BatchExecutionPersistencePort
import com.fx.api.domain.BatchJobExecution
import com.fx.api.domain.BatchStepExecution
import com.fx.api.domain.LastJobExecution
import com.fx.api.exception.BatchException
import com.fx.api.exception.errorcode.BatchErrorCode
import com.fx.common.annotation.PersistenceAdapter
import com.fx.common.domain.batch.BatchJobParameters
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.time.LocalDateTime

/**
 * Spring Batch 메타데이터 테이블은 프레임워크(crawler) 소유라 엔티티가 없으므로 JDBC 로 읽는다.
 * api 는 Spring Batch 에 의존하지 않는다.
 */
@PersistenceAdapter
class BatchExecutionPersistenceAdapter(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) : BatchExecutionPersistencePort {

    /** 자동 실행마다 crawler 가 남기는 `scheduleKey` 파라미터로 스케줄의 실행을 찾는다. */
    override fun findLastExecutions(scheduleKeys: Collection<String>): Map<String, LastJobExecution> {
        if (scheduleKeys.isEmpty()) {
            return emptyMap()
        }

        return jdbcTemplate.query(
            """
            SELECT latest.schedule_key, e.JOB_EXECUTION_ID, e.STATUS
            FROM (
                SELECT PARAMETER_VALUE AS schedule_key, MAX(JOB_EXECUTION_ID) AS job_execution_id
                FROM BATCH_JOB_EXECUTION_PARAMS
                WHERE PARAMETER_NAME = :parameterName AND PARAMETER_VALUE IN (:scheduleKeys)
                GROUP BY PARAMETER_VALUE
            ) latest
            JOIN BATCH_JOB_EXECUTION e ON e.JOB_EXECUTION_ID = latest.job_execution_id
            """,
            mapOf("parameterName" to BatchJobParameters.SCHEDULE_KEY, "scheduleKeys" to scheduleKeys),
        ) { rs, _ ->
            rs.getString("schedule_key") to LastJobExecution(rs.getLong("JOB_EXECUTION_ID"), rs.getString("STATUS"))
        }.toMap()
    }

    /** 실행 한 페이지 → 전체 건수(마지막 페이지면 생략) → 그 페이지의 파라미터 순으로 읽는다. */
    override fun findExecutions(jobName: String?, status: String?, pageable: Pageable): Page<BatchJobExecution> {
        val conditions = buildList {
            if (jobName != null) {
                add("i.JOB_NAME = :jobName")
            }
            if (status != null) {
                add("e.STATUS = :status")
            }
        }
        val where = if (conditions.isEmpty()) "" else conditions.joinToString(" AND ", prefix = "WHERE ")
        val params = mapOf(
            "jobName" to jobName,
            "status" to status,
            "limit" to pageable.pageSize,
            "offset" to pageable.offset,
        )

        val rows = jdbcTemplate.query(
            """
            $SELECT_EXECUTION
            $where
            ORDER BY e.JOB_EXECUTION_ID DESC
            LIMIT :limit OFFSET :offset
            """,
            params,
            executionRowMapper,
        )
        val parameters = findParameters(rows.map(ExecutionRow::id))
        val content = rows.map {
            it.toExecution(parameters[it.id].orEmpty())
        }

        return PageableExecutionUtils.getPage(content, pageable) {
            jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM BATCH_JOB_EXECUTION e
                JOIN BATCH_JOB_INSTANCE i ON i.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID
                $where
                """,
                params,
                Long::class.java,
            ) ?: 0L
        }
    }

    override fun getExecution(executionId: Long): BatchJobExecution {
        val row = jdbcTemplate.query(
            "$SELECT_EXECUTION WHERE e.JOB_EXECUTION_ID = :id",
            mapOf("id" to executionId),
            executionRowMapper,
        ).singleOrNull() ?: throw BatchException(BatchErrorCode.EXECUTION_NOT_FOUND)

        return row.toExecution(findParameters(listOf(row.id))[row.id].orEmpty())
    }

    override fun findSteps(executionId: Long): List<BatchStepExecution> =
        jdbcTemplate.query(
            """
            SELECT STEP_NAME, STATUS, READ_COUNT, WRITE_COUNT, FILTER_COUNT,
                   READ_SKIP_COUNT, PROCESS_SKIP_COUNT, WRITE_SKIP_COUNT, COMMIT_COUNT, ROLLBACK_COUNT,
                   START_TIME, END_TIME, EXIT_MESSAGE
            FROM BATCH_STEP_EXECUTION
            WHERE JOB_EXECUTION_ID = :id
            ORDER BY STEP_EXECUTION_ID
            """,
            mapOf("id" to executionId),
        ) { rs, _ ->
            BatchStepExecution(
                stepName = rs.getString("STEP_NAME"),
                status = rs.getString("STATUS"),
                readCount = rs.getLong("READ_COUNT"),
                writeCount = rs.getLong("WRITE_COUNT"),
                filterCount = rs.getLong("FILTER_COUNT"),
                skipCount = rs.getLong("READ_SKIP_COUNT") + rs.getLong("PROCESS_SKIP_COUNT") + rs.getLong("WRITE_SKIP_COUNT"),
                commitCount = rs.getLong("COMMIT_COUNT"),
                rollbackCount = rs.getLong("ROLLBACK_COUNT"),
                startTime = rs.localDateTime("START_TIME"),
                endTime = rs.localDateTime("END_TIME"),
                exitMessage = rs.getString("EXIT_MESSAGE"),
            )
        }

    /** 실행별 파라미터. 값은 crawler 가 문자열로 남긴 그대로다. */
    private fun findParameters(executionIds: List<Long>): Map<Long, Map<String, String>> {
        if (executionIds.isEmpty()) {
            return emptyMap()
        }

        return jdbcTemplate.query(
            """
            SELECT JOB_EXECUTION_ID, PARAMETER_NAME, PARAMETER_VALUE
            FROM BATCH_JOB_EXECUTION_PARAMS
            WHERE JOB_EXECUTION_ID IN (:ids)
            """,
            mapOf("ids" to executionIds),
        ) { rs, _ ->
            Triple(rs.getLong("JOB_EXECUTION_ID"), rs.getString("PARAMETER_NAME"), rs.getString("PARAMETER_VALUE").orEmpty())
        }
            .groupBy(
                {
                    it.first
                },
                {
                    it.second to it.third
                },
            )
            .mapValues { (_, parameters) ->
                parameters.toMap()
            }
    }

    /** 파라미터를 붙이기 전의 실행 행 */
    private class ExecutionRow(
        val id: Long,
        val jobName: String,
        val status: String,
        val exitCode: String?,
        val exitMessage: String?,
        val startTime: LocalDateTime?,
        val endTime: LocalDateTime?,
    ) {

        fun toExecution(parameters: Map<String, String>): BatchJobExecution =
            BatchJobExecution(id, jobName, parameters, status, exitCode, exitMessage, startTime, endTime)

    }

    private val executionRowMapper =
        RowMapper { rs, _ ->
            ExecutionRow(
                id = rs.getLong("JOB_EXECUTION_ID"),
                jobName = rs.getString("JOB_NAME"),
                status = rs.getString("STATUS"),
                exitCode = rs.getString("EXIT_CODE"),
                exitMessage = rs.getString("EXIT_MESSAGE"),
                startTime = rs.localDateTime("START_TIME"),
                endTime = rs.localDateTime("END_TIME"),
            )
        }

    private fun ResultSet.localDateTime(column: String): LocalDateTime? =
        getObject(column, LocalDateTime::class.java)

    companion object {

        private const val SELECT_EXECUTION = """
            SELECT e.JOB_EXECUTION_ID, i.JOB_NAME, e.STATUS, e.EXIT_CODE, e.EXIT_MESSAGE, e.START_TIME, e.END_TIME
            FROM BATCH_JOB_EXECUTION e
            JOIN BATCH_JOB_INSTANCE i ON i.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID
        """

    }

}
