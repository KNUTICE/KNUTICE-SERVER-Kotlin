package com.fx.api.adapter.`in`.web.batch.dto

import jakarta.validation.constraints.NotNull

data class BatchScheduleEnabledRequest(

    @field:NotNull(message = "자동 실행 여부는 필수입니다.")
    val enabled: Boolean?,

)
