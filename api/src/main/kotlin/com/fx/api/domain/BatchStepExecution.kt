package com.fx.api.domain

import java.time.LocalDateTime

/** Job 실행 안의 Step 실행 하나 (`BATCH_STEP_EXECUTION`). 건수는 Spring Batch 가 센 값 그대로다. */
data class BatchStepExecution(
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
)
