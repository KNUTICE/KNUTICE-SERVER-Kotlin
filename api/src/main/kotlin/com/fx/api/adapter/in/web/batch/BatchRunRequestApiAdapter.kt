package com.fx.api.adapter.`in`.web.batch

import com.fx.api.adapter.`in`.web.batch.dto.BatchRunRequestCreateRequest
import com.fx.api.adapter.`in`.web.batch.dto.BatchRunRequestPageResponse
import com.fx.api.adapter.`in`.web.batch.dto.BatchRunRequestResponse
import com.fx.api.adapter.`in`.web.batch.dto.BatchRunRequestSearchParam
import com.fx.api.application.port.`in`.batch.BatchRunRequestCommandUseCase
import com.fx.api.application.port.`in`.batch.BatchRunRequestQueryUseCase
import com.fx.api.config.security.dto.AuthenticatedUser
import com.fx.common.annotation.hexagonal.WebInputAdapter
import io.github.seob7.Api
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

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
        @ModelAttribute @Valid param: BatchRunRequestSearchParam
    ): ResponseEntity<Api<BatchRunRequestPageResponse>> =
        Api.OK(
            BatchRunRequestPageResponse.from(batchRunRequestQueryUseCase.getRunRequests(param.toCommand())),
            "실행 요청 조회 성공"
        )

}
