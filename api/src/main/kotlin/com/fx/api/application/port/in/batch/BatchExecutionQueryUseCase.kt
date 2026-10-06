package com.fx.api.application.port.`in`.batch

import com.fx.api.domain.BatchJobExecution
import com.fx.api.domain.BatchJobExecutionDetail
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface BatchExecutionQueryUseCase {

    /** 최신순. [jobName] · [status] 가 없으면 거르지 않는다. */
    fun getExecutions(jobName: String?, status: String?, pageable: Pageable): Page<BatchJobExecution>

    fun getExecution(executionId: Long): BatchJobExecutionDetail

}
