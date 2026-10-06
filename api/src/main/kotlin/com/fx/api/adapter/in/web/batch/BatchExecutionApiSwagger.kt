package com.fx.api.adapter.`in`.web.batch

import com.fx.api.adapter.`in`.web.batch.dto.BatchExecutionDetailResponse
import com.fx.api.adapter.`in`.web.batch.dto.BatchExecutionResponse
import com.fx.api.exception.errorcode.BatchErrorCode
import com.fx.common.annotation.ApiExceptionExplanation
import com.fx.common.annotation.ApiResponseExplanations
import com.fx.persistence.request.PagingRequest
import com.fx.persistence.response.PageResponse
import io.github.seob7.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "배치 실행 이력 API - ADMIN")
interface BatchExecutionApiSwagger {

    @Operation(summary = "실행 이력 목록", description = "Job 실행을 최신순으로 페이지 단위 조회합니다. page 는 1부터, size 는 1~100(기본 20)입니다.<br>" +
            "실행 기록은 7일만 남습니다. 열람실 Job 이 매분 실행되므로 페이지를 넘기는 사이 몇 건이 겹쳐 보일 수 있습니다.<br>" +
            "parameters 는 업무 파라미터만 담고, 자동 실행이면 scheduleKey, 수동 실행이면 requestedBy 가 있습니다.")
    fun getExecutions(
        @Parameter(description = "Job 이름 (예: noticeCrawlJob). 없으면 모든 Job") @RequestParam(required = false) jobName: String?,
        @Parameter(description = "Spring Batch 상태 (COMPLETED · FAILED · STARTED · STARTING · STOPPED · ABANDONED). 없으면 모든 상태")
        @RequestParam(required = false) status: String?,
        @ParameterObject pagingRequest: PagingRequest,
    ): ResponseEntity<Api<PageResponse<BatchExecutionResponse>>>

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "조회 실패",
                description = "없거나 보관 기간(7일)이 지나 지워진 실행인 경우",
                value = BatchErrorCode::class,
                constant = "EXECUTION_NOT_FOUND"
            ),
        ]
    )
    @Operation(summary = "실행 상세", description = "실행 하나의 Step 별 처리 건수와 오류 메시지를 조회합니다. Step 은 실행 순서입니다.")
    fun getExecution(@PathVariable executionId: Long): ResponseEntity<Api<BatchExecutionDetailResponse>>

}
