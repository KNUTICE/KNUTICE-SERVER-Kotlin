package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.BatchRunRequestQueryRepository
import com.fx.api.application.port.out.batch.BatchRunRequestPersistencePort
import com.fx.common.adapter.out.persistence.repository.BatchRunRequestRepository
import com.fx.common.annotation.PersistenceAdapter
import com.fx.common.domain.batch.BatchRunRequest
import com.fx.common.domain.batch.BatchRunRequestStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@PersistenceAdapter
class BatchRunRequestPersistenceAdapter(
    private val batchRunRequestRepository: BatchRunRequestRepository,
    private val batchRunRequestQueryRepository: BatchRunRequestQueryRepository,
) : BatchRunRequestPersistencePort {

    override fun save(request: BatchRunRequest): BatchRunRequest =
        batchRunRequestRepository.save(request)

    override fun findRequested(jobName: String): List<BatchRunRequest> =
        batchRunRequestRepository.findAllByStatusAndJobName(BatchRunRequestStatus.REQUESTED, jobName)

    override fun findRequests(status: BatchRunRequestStatus?, pageable: Pageable): Page<BatchRunRequest> =
        batchRunRequestQueryRepository.findRequests(status, pageable)

}
