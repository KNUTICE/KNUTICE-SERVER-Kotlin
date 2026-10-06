package com.fx.api.adapter.`in`.web.batch

import com.fx.api.adapter.`in`.web.batch.dto.BatchExecutionDetailResponse
import com.fx.api.adapter.`in`.web.batch.dto.BatchExecutionResponse
import com.fx.api.application.port.`in`.batch.BatchExecutionQueryUseCase
import com.fx.common.annotation.hexagonal.WebInputAdapter
import com.fx.persistence.request.PagingRequest
import com.fx.persistence.response.PageResponse
import io.github.seob7.Api
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@WebInputAdapter
@RequestMapping("/api/v1/batch/executions")
class BatchExecutionApiAdapter(
    private val batchExecutionQueryUseCase: BatchExecutionQueryUseCase,
) : BatchExecutionApiSwagger {

    @GetMapping
    override fun getExecutions(
        @RequestParam(required = false) jobName: String?,
        @RequestParam(required = false) status: String?,
        @Valid @ParameterObject pagingRequest: PagingRequest,
    ): ResponseEntity<Api<PageResponse<BatchExecutionResponse>>> {
        val executionPage = batchExecutionQueryUseCase.getExecutions(jobName, status, pagingRequest.toPageable())
        return Api.OK(
            PageResponse.from(executionPage) {
                BatchExecutionResponse.from(it)
            },
            "실행 이력 조회 성공"
        )
    }

    @GetMapping("/{executionId}")
    override fun getExecution(@PathVariable executionId: Long): ResponseEntity<Api<BatchExecutionDetailResponse>> =
        Api.OK(BatchExecutionDetailResponse.from(batchExecutionQueryUseCase.getExecution(executionId)), "실행 상세 조회 성공")

}
