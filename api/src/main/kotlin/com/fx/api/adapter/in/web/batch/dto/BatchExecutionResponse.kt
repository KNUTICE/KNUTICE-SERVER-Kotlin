package com.fx.api.adapter.`in`.web.batch.dto

import com.fx.api.domain.BatchJobExecution
import com.fx.common.domain.batch.BatchTriggerType
import java.time.LocalDateTime

data class BatchExecutionResponse(
    val id: Long,
    val jobName: String,
    /** 업무 파라미터 (예: topicType) */
    val parameters: Map<String, String>,
    val triggerType: BatchTriggerType?,
    /** 자동 실행이면 스케줄 키 */
    val scheduleKey: String?,
    /** 수동 실행이면 요청한 관리자 */
    val requestedBy: String?,
    /** Spring Batch 상태 (COMPLETED · FAILED · STARTED 등) */
    val status: String,
    val exitCode: String?,
    val startTime: LocalDateTime?,
    /** 실행 중이면 null */
    val endTime: LocalDateTime?,
    /** 실행 중이면 null */
    val durationMs: Long?,
) {

    companion object {

        fun from(execution: BatchJobExecution): BatchExecutionResponse =
            BatchExecutionResponse(
                id = execution.id,
                jobName = execution.jobName,
                parameters = execution.parameters,
                triggerType = execution.triggerType,
                scheduleKey = execution.scheduleKey,
                requestedBy = execution.requestedBy,
                status = execution.status,
                exitCode = execution.exitCode,
                startTime = execution.startTime,
                endTime = execution.endTime,
                durationMs = execution.durationMs,
            )

    }

}
