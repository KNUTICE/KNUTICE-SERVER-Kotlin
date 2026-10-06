package com.fx.api.application.port.`in`.batch

import com.fx.common.domain.batch.BatchRunRequest
import com.fx.common.domain.batch.BatchRunRequestStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface BatchRunRequestQueryUseCase {

    /** 최신순. [status] 가 없으면 모든 상태 */
    fun getRunRequests(status: BatchRunRequestStatus?, pageable: Pageable): Page<BatchRunRequest>

}
