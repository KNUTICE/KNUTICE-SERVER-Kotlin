package com.fx.api.adapter.out.persistence

import com.fx.api.application.port.out.batch.BatchExecutionPersistencePort
import com.fx.api.domain.LastJobExecution
import com.fx.common.annotation.PersistenceAdapter
import com.fx.common.domain.batch.BatchJobParameters
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

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

}
