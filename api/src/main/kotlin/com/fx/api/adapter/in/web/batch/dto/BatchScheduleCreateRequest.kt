package com.fx.api.adapter.`in`.web.batch.dto

import com.fx.api.application.port.`in`.batch.dto.BatchScheduleCreateCommand
import com.fx.common.domain.batch.BATCH_SCHEDULE_DESCRIPTION_MAX_LENGTH
import com.fx.common.domain.batch.BATCH_SCHEDULE_KEY_MAX_LENGTH
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class BatchScheduleCreateRequest(

    @field:NotBlank(message = "스케줄 키는 필수입니다.")
    @field:Size(max = BATCH_SCHEDULE_KEY_MAX_LENGTH, message = "스케줄 키는 {max}자 이하여야 합니다.")
    @field:Pattern(regexp = "^[a-z0-9-]*$", message = "스케줄 키는 영문 소문자 · 숫자 · 하이픈(-)만 쓸 수 있습니다.")
    val scheduleKey: String,

    @field:NotBlank(message = "Job 이름은 필수입니다.")
    val jobName: String,

    /** 파라미터가 없는 Job 은 `{}` */
    @field:NotNull(message = "Job 파라미터는 필수입니다. 파라미터가 없는 Job 은 빈 객체를 보내 주세요.")
    val jobParameters: Map<String, String>?,

    @field:NotBlank(message = "cron 은 필수입니다.")
    val cron: String,

    @field:NotNull(message = "자동 실행 여부는 필수입니다.")
    val enabled: Boolean?,

    @field:NotBlank(message = "설명은 필수입니다.")
    @field:Size(max = BATCH_SCHEDULE_DESCRIPTION_MAX_LENGTH, message = "설명은 {max}자 이하여야 합니다.")
    val description: String,

) {

    fun toCommand(): BatchScheduleCreateCommand =
        BatchScheduleCreateCommand(
            scheduleKey = scheduleKey,
            jobName = jobName,
            jobParameters = requireNotNull(jobParameters),
            cron = cron,
            enabled = requireNotNull(enabled),
            description = description,
        )

}
