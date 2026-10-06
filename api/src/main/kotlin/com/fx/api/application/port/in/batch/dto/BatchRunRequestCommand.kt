package com.fx.api.application.port.`in`.batch.dto

data class BatchRunRequestCommand(
    val jobName: String,
    val jobParameters: Map<String, String>,
    /** 요청한 관리자 */
    val userId: Long,
)
