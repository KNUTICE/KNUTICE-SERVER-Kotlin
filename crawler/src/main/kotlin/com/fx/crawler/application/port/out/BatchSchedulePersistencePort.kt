package com.fx.crawler.application.port.out

import com.fx.common.domain.batch.BatchSchedule
import java.time.LocalDateTime

interface BatchSchedulePersistencePort {

    /** 켜져 있지만 다음 발화 시각을 아직 계산하지 않은 스케줄. */
    fun findUninitialized(): List<BatchSchedule>

    fun initializeNextFireAt(scheduleId: Long, next: LocalDateTime, now: LocalDateTime): Boolean

    /** 켜져 있고 발화 시각이 [now] 이전인 스케줄. */
    fun findDue(now: LocalDateTime): List<BatchSchedule>

    /** 발화를 선점한다. 다른 인스턴스가 먼저 가져갔으면 false. */
    fun claim(scheduleId: Long, previous: LocalDateTime, next: LocalDateTime, now: LocalDateTime): Boolean

}
