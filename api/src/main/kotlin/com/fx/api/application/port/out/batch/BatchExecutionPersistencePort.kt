package com.fx.api.application.port.out.batch

import com.fx.api.domain.BatchExecutionCounts
import com.fx.api.domain.BatchJobExecution
import com.fx.api.domain.BatchStepExecution
import com.fx.api.domain.LastJobExecution
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

/** crawler 가 남긴 Spring Batch 실행 기록(`BATCH_*` 테이블)을 읽는다. */
interface BatchExecutionPersistencePort {

    /** 스케줄 키별로 그 스케줄이 마지막으로 시작한 실행. 실행 기록이 없는 키는 결과에 없다. */
    fun findLastExecutions(scheduleKeys: Collection<String>): Map<String, LastJobExecution>

    /** 최신순. [jobName] · [status] 가 없으면 거르지 않는다. */
    fun findExecutions(jobName: String?, status: String?, pageable: Pageable): Page<BatchJobExecution>

    /** @throws com.fx.api.exception.BatchException 없거나 보관 기간이 지나 지워진 실행 (EXECUTION_NOT_FOUND) */
    fun getExecution(executionId: Long): BatchJobExecution

    /** 실행 순서 */
    fun findSteps(executionId: Long): List<BatchStepExecution>

    /** [failedSince] 이후에 실패로 끝난 실행 수와 지금 실행 중인 실행 수 */
    fun countFailedAndRunning(failedSince: LocalDateTime): BatchExecutionCounts

}
