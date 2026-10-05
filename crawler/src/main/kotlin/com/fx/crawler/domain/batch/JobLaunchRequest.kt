package com.fx.crawler.domain.batch

import java.time.LocalDateTime

/**
 * Job 실행 요청.
 * @property parameters 스케줄 · 수동 요청에 적힌 업무 파라미터 (예: `topicType`)
 */
data class JobLaunchRequest(
    val jobName: String,
    val parameters: Map<String, String>,
    val trigger: JobTrigger,
)

sealed interface JobTrigger {

    /** 스케줄에 따른 자동 실행 */
    data class Scheduled(val scheduleKey: String, val scheduledAt: LocalDateTime) : JobTrigger

    /** 관리자 수동 실행 */
    data class Manual(val requestId: Long, val requestedBy: String) : JobTrigger

}
