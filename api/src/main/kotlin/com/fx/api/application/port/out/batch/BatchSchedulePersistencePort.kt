package com.fx.api.application.port.out.batch

import com.fx.common.domain.batch.BatchSchedule

interface BatchSchedulePersistencePort {

    /** scheduleKey 순. */
    fun findAll(): List<BatchSchedule>

    /** @throws com.fx.api.exception.BatchException 없는 스케줄 (SCHEDULE_NOT_FOUND) */
    fun getByScheduleKey(scheduleKey: String): BatchSchedule

    fun existsByScheduleKey(scheduleKey: String): Boolean

    /** @throws com.fx.api.exception.BatchException 이미 있는 스케줄 키 (SCHEDULE_KEY_DUPLICATED) */
    fun create(schedule: BatchSchedule): BatchSchedule

}
