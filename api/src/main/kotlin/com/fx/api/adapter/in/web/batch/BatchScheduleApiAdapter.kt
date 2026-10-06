package com.fx.api.adapter.`in`.web.batch

import com.fx.api.adapter.`in`.web.batch.dto.BatchScheduleCreateRequest
import com.fx.api.adapter.`in`.web.batch.dto.BatchScheduleEnabledRequest
import com.fx.api.adapter.`in`.web.batch.dto.BatchScheduleResponse
import com.fx.api.adapter.`in`.web.batch.dto.BatchScheduleUpdateRequest
import com.fx.api.application.port.`in`.batch.BatchScheduleCommandUseCase
import com.fx.api.application.port.`in`.batch.BatchScheduleQueryUseCase
import com.fx.common.annotation.hexagonal.WebInputAdapter
import io.github.seob7.Api
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@WebInputAdapter
@RequestMapping("/api/v1/batch/schedules")
class BatchScheduleApiAdapter(
    private val batchScheduleQueryUseCase: BatchScheduleQueryUseCase,
    private val batchScheduleCommandUseCase: BatchScheduleCommandUseCase,
) : BatchScheduleApiSwagger {

    @GetMapping
    override fun getSchedules(): ResponseEntity<Api<List<BatchScheduleResponse>>> =
        Api.OK(BatchScheduleResponse.from(batchScheduleQueryUseCase.getSchedules()), "스케줄 조회 성공")

    @PostMapping
    override fun createSchedule(
        @RequestBody @Valid request: BatchScheduleCreateRequest
    ): ResponseEntity<Api<BatchScheduleResponse>> =
        Api.OK(
            BatchScheduleResponse.from(batchScheduleCommandUseCase.createSchedule(request.toCommand())),
            "스케줄을 추가했습니다."
        )

    @PatchMapping("/{scheduleKey}")
    override fun updateSchedule(
        @PathVariable scheduleKey: String,
        @RequestBody @Valid request: BatchScheduleUpdateRequest
    ): ResponseEntity<Api<BatchScheduleResponse>> =
        Api.OK(
            BatchScheduleResponse.from(batchScheduleCommandUseCase.updateSchedule(request.toCommand(scheduleKey))),
            "스케줄을 수정했습니다."
        )

    @PatchMapping("/{scheduleKey}/enabled")
    override fun changeEnabled(
        @PathVariable scheduleKey: String,
        @RequestBody @Valid request: BatchScheduleEnabledRequest
    ): ResponseEntity<Api<BatchScheduleResponse>> {
        val enabled = requireNotNull(request.enabled)
        return Api.OK(
            BatchScheduleResponse.from(batchScheduleCommandUseCase.changeEnabled(scheduleKey, enabled)),
            if (enabled) "자동 실행을 켰습니다." else "자동 실행을 껐습니다."
        )
    }

}
