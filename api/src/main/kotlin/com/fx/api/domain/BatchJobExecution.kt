package com.fx.api.domain

import com.fx.common.domain.batch.BatchJobParameters
import com.fx.common.domain.batch.BatchTriggerType
import java.time.Duration
import java.time.LocalDateTime

/**
 * crawler 가 남긴 Job 실행 기록 하나 (`BATCH_JOB_EXECUTION`).
 *
 * [allParameters] 에는 업무 파라미터(예: `topicType`)와 폴러가 붙이는 파라미터(`triggerType`, `scheduleKey` 등)가 섞여 있다.
 * [status] · [exitCode] 는 Spring Batch 값 그대로다 (예: COMPLETED · FAILED · STARTED).
 */
class BatchJobExecution(
    val id: Long,
    val jobName: String,
    private val allParameters: Map<String, String>,
    val status: String,
    val exitCode: String?,
    val exitMessage: String?,
    val startTime: LocalDateTime?,
    /** 실행 중이면 null */
    val endTime: LocalDateTime?,
) {

    /** 업무 파라미터. 폴러가 붙이는 파라미터는 뺀다. */
    val parameters: Map<String, String>
        get() = allParameters - BatchJobParameters.RESERVED_NAMES

    val triggerType: BatchTriggerType?
        get() = BatchTriggerType.from(allParameters[BatchJobParameters.TRIGGER_TYPE])

    /** 자동 실행한 스케줄 키. 수동 실행이면 null */
    val scheduleKey: String?
        get() = allParameters[BatchJobParameters.SCHEDULE_KEY]

    /** 수동 실행을 요청한 관리자. 자동 실행이면 null */
    val requestedBy: String?
        get() = allParameters[BatchJobParameters.REQUESTED_BY]

    /** 실행 중이면 null */
    val durationMs: Long?
        get() = if (startTime != null && endTime != null) Duration.between(startTime, endTime).toMillis() else null

}
