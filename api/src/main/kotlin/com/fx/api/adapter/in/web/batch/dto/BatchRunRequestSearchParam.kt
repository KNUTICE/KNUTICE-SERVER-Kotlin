package com.fx.api.adapter.`in`.web.batch.dto

import com.fx.api.application.port.`in`.batch.dto.BatchRunRequestSearchCommand
import com.fx.common.domain.batch.BatchRunRequestStatus
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

/** 수동 실행 요청 목록 조회 조건. [cursor] 는 직전 응답의 `nextCursor` 다. */
data class BatchRunRequestSearchParam(

    val status: BatchRunRequestStatus? = null,

    val cursor: Long? = null,

    @field:Min(value = 1, message = "size 는 {value} 이상이어야 합니다.")
    @field:Max(value = 100, message = "size 는 {value} 이하여야 합니다.")
    val size: Int = 20,

) {

    fun toCommand(): BatchRunRequestSearchCommand =
        BatchRunRequestSearchCommand(
            status = status,
            cursor = cursor,
            size = size,
        )

}
