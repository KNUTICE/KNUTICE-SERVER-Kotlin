package com.fx.crawler.adapter.out.persistence

import com.fx.common.annotation.PersistenceAdapter
import com.fx.crawler.application.port.out.BatchMetadataPort
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDateTime

/**
 * Spring Batch 메타데이터 테이블은 프레임워크 소유라 엔티티가 없으므로 JDBC 로 지운다.
 * 외래 키 역순(StepExecution 컨텍스트 → StepExecution → JobExecution 컨텍스트 · 파라미터 → JobExecution)으로 지운다.
 * 끝나지 않은 JobExecution(`END_TIME` 이 없음)은 건드리지 않는다.
 */
@PersistenceAdapter
class BatchMetadataPersistenceAdapter(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) : BatchMetadataPort {

    override fun deleteJobExecutionsEndedBefore(cutoff: LocalDateTime, limit: Int): Int {
        val jobExecutionIds = jdbcTemplate.queryForList(
            """
            SELECT JOB_EXECUTION_ID FROM BATCH_JOB_EXECUTION
            WHERE END_TIME < :cutoff
            ORDER BY JOB_EXECUTION_ID
            LIMIT :limit
            """,
            mapOf("cutoff" to cutoff, "limit" to limit),
            Long::class.java,
        )
        if (jobExecutionIds.isEmpty()) {
            return 0
        }

        val ids = mapOf("ids" to jobExecutionIds)
        jdbcTemplate.update(
            """
            DELETE c FROM BATCH_STEP_EXECUTION_CONTEXT c
            JOIN BATCH_STEP_EXECUTION s ON s.STEP_EXECUTION_ID = c.STEP_EXECUTION_ID
            WHERE s.JOB_EXECUTION_ID IN (:ids)
            """,
            ids,
        )
        jdbcTemplate.update("DELETE FROM BATCH_STEP_EXECUTION WHERE JOB_EXECUTION_ID IN (:ids)", ids)
        jdbcTemplate.update("DELETE FROM BATCH_JOB_EXECUTION_CONTEXT WHERE JOB_EXECUTION_ID IN (:ids)", ids)
        jdbcTemplate.update("DELETE FROM BATCH_JOB_EXECUTION_PARAMS WHERE JOB_EXECUTION_ID IN (:ids)", ids)
        return jdbcTemplate.update("DELETE FROM BATCH_JOB_EXECUTION WHERE JOB_EXECUTION_ID IN (:ids)", ids)
    }

    override fun deleteOrphanJobInstances(limit: Int): Int =
        jdbcTemplate.update(
            """
            DELETE FROM BATCH_JOB_INSTANCE
            WHERE NOT EXISTS (
                SELECT 1 FROM BATCH_JOB_EXECUTION e WHERE e.JOB_INSTANCE_ID = BATCH_JOB_INSTANCE.JOB_INSTANCE_ID
            )
            LIMIT :limit
            """,
            mapOf("limit" to limit),
        )

}
