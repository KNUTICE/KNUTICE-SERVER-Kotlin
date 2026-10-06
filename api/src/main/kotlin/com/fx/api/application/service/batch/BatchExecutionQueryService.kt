package com.fx.api.application.service.batch

import com.fx.api.application.port.`in`.batch.BatchExecutionQueryUseCase
import com.fx.api.application.port.out.batch.BatchExecutionPersistencePort
import com.fx.api.domain.BatchJobExecution
import com.fx.api.domain.BatchJobExecutionDetail
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** crawler 가 남긴 Job 실행 이력 조회. 기록은 crawler 의 정리 Job 이 7일만 남긴다. */
@Service
@Transactional(readOnly = true)
class BatchExecutionQueryService(
    private val batchExecutionPersistencePort: BatchExecutionPersistencePort,
) : BatchExecutionQueryUseCase {

    override fun getExecutions(jobName: String?, status: String?, pageable: Pageable): Page<BatchJobExecution> =
        batchExecutionPersistencePort.findExecutions(jobName, status, pageable)

    override fun getExecution(executionId: Long): BatchJobExecutionDetail =
        BatchJobExecutionDetail(
            execution = batchExecutionPersistencePort.getExecution(executionId),
            steps = batchExecutionPersistencePort.findSteps(executionId),
        )

}
