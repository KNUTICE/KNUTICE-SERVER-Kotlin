package com.fx.api.adapter.`in`.web.batch

import com.fx.api.adapter.`in`.web.batch.dto.BatchScheduleCreateRequest
import com.fx.api.adapter.`in`.web.batch.dto.BatchScheduleEnabledRequest
import com.fx.api.adapter.`in`.web.batch.dto.BatchScheduleResponse
import com.fx.api.adapter.`in`.web.batch.dto.BatchScheduleUpdateRequest
import com.fx.api.exception.errorcode.BatchErrorCode
import com.fx.common.annotation.ApiExceptionExplanation
import com.fx.common.annotation.ApiResponseExplanations
import io.github.seob7.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "배치 스케줄 관리 API - ADMIN")
interface BatchScheduleApiSwagger {

    @Operation(summary = "스케줄 목록", description = "모든 스케줄을 scheduleKey 순으로 조회합니다.<br>" +
            "lastExecution 은 이 스케줄이 마지막으로 시작한 실행입니다. 실행 기록은 7일만 남으므로 없을 수 있습니다.")
    fun getSchedules(): ResponseEntity<Api<List<BatchScheduleResponse>>>

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "추가 실패 - 스케줄 키",
                description = "이미 있는 scheduleKey 인 경우",
                value = BatchErrorCode::class,
                constant = "SCHEDULE_KEY_DUPLICATED"
            ),
            ApiExceptionExplanation(
                name = "추가 실패 - Job",
                description = "등록되지 않은 jobName 인 경우",
                value = BatchErrorCode::class,
                constant = "JOB_NOT_FOUND"
            ),
            ApiExceptionExplanation(
                name = "추가 실패 - 파라미터",
                description = "필요한 파라미터가 없거나, 받지 않는 파라미터가 있거나, 값이 잘못된 경우. 메시지에 이유를 담습니다.",
                value = BatchErrorCode::class,
                constant = "JOB_PARAMETERS_INVALID"
            ),
            ApiExceptionExplanation(
                name = "추가 실패 - cron",
                description = "형식이 잘못됐거나, 초 필드가 0 이 아니거나, 다음 실행 시각이 없는 경우. 메시지에 이유를 담습니다.",
                value = BatchErrorCode::class,
                constant = "CRON_INVALID"
            ),
        ]
    )
    @Operation(summary = "스케줄 추가", description = "Job 을 자동 실행할 스케줄을 추가합니다. 같은 Job 을 파라미터만 바꿔 여러 스케줄로 둘 수 있습니다.<br>" +
            "cron 은 Spring 6자리(초 분 시 일 월 요일, Asia/Seoul)이고 초는 0 이어야 합니다.<br>" +
            "Job 별 파라미터: noticeCrawlJob 은 topicType(NOTICE · MAJOR), maintenanceJob 은 retentionDays(1 이상 정수), 나머지는 없음")
    fun createSchedule(@RequestBody request: BatchScheduleCreateRequest): ResponseEntity<Api<BatchScheduleResponse>>

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "수정 실패 - 스케줄",
                description = "없는 scheduleKey 인 경우",
                value = BatchErrorCode::class,
                constant = "SCHEDULE_NOT_FOUND"
            ),
            ApiExceptionExplanation(
                name = "수정 실패 - 파라미터",
                description = "필요한 파라미터가 없거나, 받지 않는 파라미터가 있거나, 값이 잘못된 경우",
                value = BatchErrorCode::class,
                constant = "JOB_PARAMETERS_INVALID"
            ),
            ApiExceptionExplanation(
                name = "수정 실패 - cron",
                description = "형식이 잘못됐거나, 초 필드가 0 이 아니거나, 다음 실행 시각이 없는 경우",
                value = BatchErrorCode::class,
                constant = "CRON_INVALID"
            ),
        ]
    )
    @Operation(summary = "스케줄 수정", description = "cron · 파라미터 · 설명 중 보낸 값만 바꿉니다.<br>" +
            "cron 을 바꾸면 다음 실행 시각을 바로 다시 계산합니다. 실행 중인 Job 에는 영향이 없습니다.")
    fun updateSchedule(
        @PathVariable scheduleKey: String,
        @RequestBody request: BatchScheduleUpdateRequest
    ): ResponseEntity<Api<BatchScheduleResponse>>

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "변경 실패",
                description = "없는 scheduleKey 인 경우",
                value = BatchErrorCode::class,
                constant = "SCHEDULE_NOT_FOUND"
            ),
        ]
    )
    @Operation(summary = "자동 실행 켜기 · 끄기", description = "켜면 지금 이후 첫 시각부터 다시 셉니다. 꺼져 있던 동안 지난 실행은 하지 않습니다.")
    fun changeEnabled(
        @PathVariable scheduleKey: String,
        @RequestBody request: BatchScheduleEnabledRequest
    ): ResponseEntity<Api<BatchScheduleResponse>>

}
