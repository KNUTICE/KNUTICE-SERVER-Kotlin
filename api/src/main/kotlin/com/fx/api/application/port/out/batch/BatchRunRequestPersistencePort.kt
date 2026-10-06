package com.fx.api.application.port.out.batch

import com.fx.common.domain.batch.BatchRunRequest
import com.fx.common.domain.batch.BatchRunRequestStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface BatchRunRequestPersistencePort {

    fun save(request: BatchRunRequest): BatchRunRequest

    /** 아직 실행하지 않은(REQUESTED) 요청 */
    fun findRequested(jobName: String): List<BatchRunRequest>

    /** 최신순. [status] 가 없으면 모든 상태 */
    fun findRequests(status: BatchRunRequestStatus?, pageable: Pageable): Page<BatchRunRequest>

}
