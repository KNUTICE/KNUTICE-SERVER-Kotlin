package com.fx.api.application.port.`in`.batch.dto

/** null 인 값은 바꾸지 않는다. */
data class BatchScheduleUpdateCommand(
    val scheduleKey: String,
    val cron: String?,
    val jobParameters: Map<String, String>?,
    val description: String?,
)
