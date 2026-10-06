package com.fx.api.domain

/** Job 실행과 그 Step 실행들 (실행 순서). */
class BatchJobExecutionDetail(
    val execution: BatchJobExecution,
    val steps: List<BatchStepExecution>,
)
