package com.fx.api.adapter.`in`.web.batch.dto

import com.fx.api.domain.BatchJobExecutionDetail
import com.fx.api.domain.BatchStepExecution
import com.fx.common.domain.batch.BatchTriggerType
import java.time.LocalDateTime

data class BatchExecutionDetailResponse(
    val id: Long,
    val jobName: String,
    val parameters: Map<String, String>,
    val triggerType: BatchTriggerType?,
    val scheduleKey: String?,
    val requestedBy: String?,
    val status: String,
    val exitCode: String?,
    /** 실패했으면 예외 메시지 · 스택 일부 */
    val exitMessage: String?,
    val startTime: LocalDateTime?,
    val endTime: LocalDateTime?,
    val durationMs: Long?,
    /** 실행 순서 */
    val steps: List<StepResponse>,
) {

    data class StepResponse(
        val stepName: String,
        val status: String,
        val readCount: Long,
        val writeCount: Long,
        val filterCount: Long,
        /** 읽기 · 처리 · 쓰기에서 건너뛴 건수의 합 */
        val skipCount: Long,
        val commitCount: Long,
        val rollbackCount: Long,
        val startTime: LocalDateTime?,
        val endTime: LocalDateTime?,
        val exitMessage: String?,
    ) {

        companion object {

            fun from(step: BatchStepExecution): StepResponse =
                StepResponse(
                    stepName = step.stepName,
                    status = step.status,
                    readCount = step.readCount,
                    writeCount = step.writeCount,
                    filterCount = step.filterCount,
                    skipCount = step.skipCount,
                    commitCount = step.commitCount,
                    rollbackCount = step.rollbackCount,
                    startTime = step.startTime,
                    endTime = step.endTime,
                    exitMessage = step.exitMessage,
                )

        }

    }

    companion object {

        fun from(detail: BatchJobExecutionDetail): BatchExecutionDetailResponse {
            val execution = detail.execution
            return BatchExecutionDetailResponse(
                id = execution.id,
                jobName = execution.jobName,
                parameters = execution.parameters,
                triggerType = execution.triggerType,
                scheduleKey = execution.scheduleKey,
                requestedBy = execution.requestedBy,
                status = execution.status,
                exitCode = execution.exitCode,
                exitMessage = execution.exitMessage,
                startTime = execution.startTime,
                endTime = execution.endTime,
                durationMs = execution.durationMs,
                steps = detail.steps.map {
                    StepResponse.from(it)
                },
            )
        }

    }

}
