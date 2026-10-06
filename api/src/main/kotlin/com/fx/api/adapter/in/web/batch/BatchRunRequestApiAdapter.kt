package com.fx.api.adapter.`in`.web.batch

import com.fx.api.adapter.`in`.web.batch.dto.BatchRunRequestCreateRequest
import com.fx.api.adapter.`in`.web.batch.dto.BatchRunRequestResponse
import com.fx.api.application.port.`in`.batch.BatchRunRequestCommandUseCase
import com.fx.api.application.port.`in`.batch.BatchRunRequestQueryUseCase
import com.fx.api.config.security.dto.AuthenticatedUser
import com.fx.common.annotation.hexagonal.WebInputAdapter
import com.fx.common.domain.batch.BatchRunRequestStatus
import com.fx.persistence.request.PagingRequest
import com.fx.persistence.response.PageResponse
import io.github.seob7.Api
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@WebInputAdapter
@RequestMapping("/api/v1/batch/run-requests")
class BatchRunRequestApiAdapter(
    private val batchRunRequestCommandUseCase: BatchRunRequestCommandUseCase,
    private val batchRunRequestQueryUseCase: BatchRunRequestQueryUseCase,
) : BatchRunRequestApiSwagger {

    @PostMapping
    override fun requestRun(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @RequestBody @Valid request: BatchRunRequestCreateRequest
    ): ResponseEntity<Api<BatchRunRequestResponse>> =
        Api.OK(
            BatchRunRequestResponse.from(batchRunRequestCommandUseCase.requestRun(request.toCommand(user.userId.toLong()))),
            "실행을 요청했습니다."
        )

    @GetMapping
    override fun getRunRequests(
        @RequestParam(required = false) status: BatchRunRequestStatus?,
        @Valid @ParameterObject pagingRequest: PagingRequest,
    ): ResponseEntity<Api<PageResponse<BatchRunRequestResponse>>> {
        val requestPage = batchRunRequestQueryUseCase.getRunRequests(status, pagingRequest.toPageable())
        return Api.OK(
            PageResponse.from(requestPage) {
                BatchRunRequestResponse.from(it)
            },
            "실행 요청 조회 성공"
        )
    }

}
