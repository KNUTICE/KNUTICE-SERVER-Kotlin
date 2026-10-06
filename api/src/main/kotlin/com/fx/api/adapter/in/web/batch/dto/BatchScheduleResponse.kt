package com.fx.api.adapter.`in`.web.batch.dto

import com.fx.api.domain.BatchScheduleDetail
import com.fx.common.domain.batch.BatchJobParameters
import java.time.LocalDateTime

data class BatchScheduleResponse(
    val scheduleKey: String,
    val jobName: String,
    val jobParameters: Map<String, String>,
    val cron: String,
    val enabled: Boolean,
    val description: String,
    /** 꺼져 있으면 null */
    val nextFireAt: LocalDateTime?,
    /** Job 을 시작한 시각. 성공 여부는 [lastExecution] 에 있다 */
    val lastFiredAt: LocalDateTime?,
    val lastExecution: LastExecutionResponse?,
) {

    data class LastExecutionResponse(
        val id: Long,
        val status: String,
    )

    companion object {

        fun from(detail: BatchScheduleDetail): BatchScheduleResponse {
            val schedule = detail.schedule
            return BatchScheduleResponse(
                scheduleKey = schedule.scheduleKey,
                jobName = schedule.jobName,
                jobParameters = BatchJobParameters.parse(schedule.jobParameters),
                cron = schedule.cron,
                enabled = schedule.enabled,
                description = schedule.description,
                nextFireAt = schedule.nextFireAt,
                lastFiredAt = schedule.lastFiredAt,
                lastExecution = detail.lastExecution?.let {
                    LastExecutionResponse(it.id, it.status)
                },
            )
        }

        fun from(details: List<BatchScheduleDetail>): List<BatchScheduleResponse> =
            details.map {
                from(it)
            }

    }

}
