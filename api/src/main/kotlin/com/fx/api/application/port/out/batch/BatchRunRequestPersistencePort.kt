package com.fx.api.application.port.out.batch

import com.fx.common.domain.batch.BatchRunRequest
import com.fx.common.domain.batch.BatchRunRequestStatus

interface BatchRunRequestPersistencePort {

    fun save(request: BatchRunRequest): BatchRunRequest

    /** 아직 실행하지 않은(REQUESTED) 요청 */
    fun findRequested(jobName: String): List<BatchRunRequest>

    /** 최신순. [cursor] 가 있으면 그보다 오래된(id 가 작은) 요청부터 [limit] 개 */
    fun findRequests(status: BatchRunRequestStatus?, cursor: Long?, limit: Int): List<BatchRunRequest>

}
