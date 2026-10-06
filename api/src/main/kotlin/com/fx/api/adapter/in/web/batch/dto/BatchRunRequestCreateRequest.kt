package com.fx.api.adapter.`in`.web.batch.dto

import com.fx.api.application.port.`in`.batch.dto.BatchRunRequestCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class BatchRunRequestCreateRequest(

    @field:NotBlank(message = "Job 이름은 필수입니다.")
    val jobName: String,

    /** 파라미터가 없는 Job 은 `{}` */
    @field:NotNull(message = "Job 파라미터는 필수입니다. 파라미터가 없는 Job 은 빈 객체를 보내 주세요.")
    val jobParameters: Map<String, String>?,

) {

    fun toCommand(userId: Long): BatchRunRequestCommand =
        BatchRunRequestCommand(
            jobName = jobName,
            jobParameters = requireNotNull(jobParameters),
            userId = userId,
        )

}
