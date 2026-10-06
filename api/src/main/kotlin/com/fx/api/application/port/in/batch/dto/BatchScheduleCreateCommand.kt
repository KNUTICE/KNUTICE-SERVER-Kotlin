package com.fx.api.application.port.`in`.batch.dto

data class BatchScheduleCreateCommand(
    val scheduleKey: String,
    val jobName: String,
    val jobParameters: Map<String, String>,
    val cron: String,
    val enabled: Boolean,
    val description: String,
)
