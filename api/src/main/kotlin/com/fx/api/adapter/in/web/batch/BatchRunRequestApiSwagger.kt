package com.fx.api.adapter.`in`.web.batch

import com.fx.api.adapter.`in`.web.batch.dto.BatchRunRequestCreateRequest
import com.fx.api.adapter.`in`.web.batch.dto.BatchRunRequestPageResponse
import com.fx.api.adapter.`in`.web.batch.dto.BatchRunRequestResponse
import com.fx.api.adapter.`in`.web.batch.dto.BatchRunRequestSearchParam
import com.fx.api.config.security.dto.AuthenticatedUser
import com.fx.api.exception.errorcode.BatchErrorCode
import com.fx.common.annotation.ApiExceptionExplanation
import com.fx.common.annotation.ApiResponseExplanations
import io.github.seob7.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "배치 수동 실행 API - ADMIN")
interface BatchRunRequestApiSwagger {

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "요청 실패 - Job",
                description = "등록되지 않은 jobName 인 경우",
                value = BatchErrorCode::class,
                constant = "JOB_NOT_FOUND"
            ),
            ApiExceptionExplanation(
                name = "요청 실패 - 파라미터",
                description = "필요한 파라미터가 없거나, 받지 않는 파라미터가 있거나, 값이 잘못된 경우. 메시지에 이유를 담습니다.",
                value = BatchErrorCode::class,
                constant = "JOB_PARAMETERS_INVALID"
            ),
            ApiExceptionExplanation(
                name = "요청 실패 - 중복",
                description = "같은 Job · 파라미터의 요청이 아직 대기 중(REQUESTED)인 경우",
                value = BatchErrorCode::class,
                constant = "RUN_REQUEST_DUPLICATED"
            ),
        ]
    )
    @Operation(summary = "수동 실행 요청", description = "Job 을 지금 한 번 실행해 달라고 요청합니다. crawler 가 최대 1분 안에 실행하거나 거절합니다.<br>" +
            "결과는 요청 목록에서 확인합니다: 실행하면 LAUNCHED + jobExecutionId, 같은 작업이 실행 중이면 REJECTED + rejectReason.<br>" +
            "requestedBy 는 로그인한 관리자의 닉네임입니다.")
    fun requestRun(
        @Parameter(hidden = true) user: AuthenticatedUser,
        @RequestBody request: BatchRunRequestCreateRequest
    ): ResponseEntity<Api<BatchRunRequestResponse>>

    @Operation(summary = "수동 실행 요청 목록", description = "최신순으로 조회합니다. 다음 페이지는 응답의 nextCursor 를 cursor 로 보냅니다 (마지막 페이지면 null).<br>" +
            "size 는 1~100, 기본 20 입니다.")
    fun getRunRequests(@ModelAttribute param: BatchRunRequestSearchParam): ResponseEntity<Api<BatchRunRequestPageResponse>>

}
