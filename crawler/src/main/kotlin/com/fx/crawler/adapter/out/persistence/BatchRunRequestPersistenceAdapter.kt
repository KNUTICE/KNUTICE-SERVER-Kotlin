package com.fx.crawler.adapter.out.persistence

import com.fx.common.adapter.out.persistence.repository.BatchRunRequestRepository
import com.fx.common.annotation.PersistenceAdapter
import com.fx.common.domain.batch.BatchRunRequest
import com.fx.common.domain.batch.BatchRunRequestStatus
import com.fx.crawler.application.port.out.BatchRunRequestPersistencePort
import org.springframework.data.domain.Limit
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@PersistenceAdapter
class BatchRunRequestPersistenceAdapter(
    private val batchRunRequestRepository: BatchRunRequestRepository,
) : BatchRunRequestPersistencePort {

    @Transactional(readOnly = true)
    override fun findRequested(limit: Int): List<BatchRunRequest> =
        batchRunRequestRepository.findAllByStatusOrderByIdAsc(BatchRunRequestStatus.REQUESTED, Limit.of(limit))

    @Transactional
    override fun claim(requestId: Long, now: LocalDateTime): Boolean =
        batchRunRequestRepository.claim(requestId, now) == 1

    @Transactional
    override fun recordJobExecution(requestId: Long, jobExecutionId: Long, now: LocalDateTime) {
        batchRunRequestRepository.updateJobExecutionId(requestId, jobExecutionId, now)
    }

    @Transactional
    override fun reject(requestId: Long, reason: String, now: LocalDateTime) {
        batchRunRequestRepository.reject(requestId, reason, now)
    }

}
