package com.fx.api.adapter.`in`.web.batch.dto

import com.fx.api.application.port.`in`.batch.dto.BatchScheduleUpdateCommand
import com.fx.common.domain.batch.BATCH_SCHEDULE_DESCRIPTION_MAX_LENGTH
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/** 보낸 필드만 바꾼다. */
data class BatchScheduleUpdateRequest(

    val cron: String? = null,

    val jobParameters: Map<String, String>? = null,

    @field:Size(max = BATCH_SCHEDULE_DESCRIPTION_MAX_LENGTH, message = "설명은 {max}자 이하여야 합니다.")
    @field:Pattern(regexp = ".*\\S.*", message = "설명은 비워 둘 수 없습니다.")
    val description: String? = null,

) {

    fun toCommand(scheduleKey: String): BatchScheduleUpdateCommand =
        BatchScheduleUpdateCommand(
            scheduleKey = scheduleKey,
            cron = cron,
            jobParameters = jobParameters,
            description = description,
        )

}
