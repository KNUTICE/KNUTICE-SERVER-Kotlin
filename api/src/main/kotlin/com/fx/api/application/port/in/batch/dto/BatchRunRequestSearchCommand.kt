package com.fx.api.application.port.`in`.batch.dto

import com.fx.common.domain.batch.BatchRunRequestStatus

data class BatchRunRequestSearchCommand(
    val status: BatchRunRequestStatus?,
    val cursor: Long?,
    val size: Int,
)
