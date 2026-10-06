package com.fx.api.domain

/** 상태별 Job 실행 수 */
data class BatchExecutionCounts(
    /** 기준 시각 이후 실패로 끝난 실행 */
    val failed: Long,
    /** 지금 실행 중(STARTING · STARTED)인 실행 */
    val running: Long,
)
