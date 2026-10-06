package com.fx.api.application.port.out.batch

import com.fx.api.domain.LastJobExecution

/** crawler 가 남긴 Spring Batch 실행 기록(`BATCH_*` 테이블)을 읽는다. */
interface BatchExecutionPersistencePort {

    /** 스케줄 키별로 그 스케줄이 마지막으로 시작한 실행. 실행 기록이 없는 키는 결과에 없다. */
    fun findLastExecutions(scheduleKeys: Collection<String>): Map<String, LastJobExecution>

}
