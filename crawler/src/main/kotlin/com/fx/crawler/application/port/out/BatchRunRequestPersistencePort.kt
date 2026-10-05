package com.fx.crawler.application.port.out

import com.fx.common.domain.batch.BatchRunRequest
import java.time.LocalDateTime

interface BatchRunRequestPersistencePort {

    /** 처리할 수동 실행 요청. 오래된 요청부터. */
    fun findRequested(limit: Int): List<BatchRunRequest>

    /** 요청을 선점한다. 다른 인스턴스가 먼저 가져갔으면 false. */
    fun claim(requestId: Long, now: LocalDateTime): Boolean

    fun recordJobExecution(requestId: Long, jobExecutionId: Long, now: LocalDateTime)

    fun reject(requestId: Long, reason: String, now: LocalDateTime)

}
