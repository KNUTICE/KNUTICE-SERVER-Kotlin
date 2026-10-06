package com.fx.api.domain

import com.fx.common.domain.batch.BatchSchedule

/** 스케줄과 그 스케줄이 마지막으로 시작한 실행. 실행 기록은 7일만 남으므로 없을 수 있다. */
class BatchScheduleDetail(
    val schedule: BatchSchedule,
    val lastExecution: LastJobExecution?,
)

/** Spring Batch 실행 하나의 ID 와 상태 (`BATCH_JOB_EXECUTION.STATUS`, 예: COMPLETED · FAILED · STARTED) */
data class LastJobExecution(
    val id: Long,
    val status: String,
)
