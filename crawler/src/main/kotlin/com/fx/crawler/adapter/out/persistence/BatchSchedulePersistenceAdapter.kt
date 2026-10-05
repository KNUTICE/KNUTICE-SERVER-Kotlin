package com.fx.crawler.adapter.out.persistence

import com.fx.common.adapter.out.persistence.repository.BatchScheduleRepository
import com.fx.common.annotation.PersistenceAdapter
import com.fx.common.domain.batch.BatchSchedule
import com.fx.crawler.application.port.out.BatchSchedulePersistencePort
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@PersistenceAdapter
class BatchSchedulePersistenceAdapter(
    private val batchScheduleRepository: BatchScheduleRepository,
) : BatchSchedulePersistencePort {

    @Transactional(readOnly = true)
    override fun findUninitialized(): List<BatchSchedule> =
        batchScheduleRepository.findAllByEnabledTrueAndNextFireAtIsNull()

    @Transactional
    override fun initializeNextFireAt(scheduleId: Long, next: LocalDateTime, now: LocalDateTime): Boolean =
        batchScheduleRepository.initializeNextFireAt(scheduleId, next, now) == 1

    @Transactional(readOnly = true)
    override fun findDue(now: LocalDateTime): List<BatchSchedule> =
        batchScheduleRepository.findAllByEnabledTrueAndNextFireAtLessThanEqualOrderByNextFireAtAsc(now)

    @Transactional
    override fun claim(scheduleId: Long, previous: LocalDateTime, next: LocalDateTime, now: LocalDateTime): Boolean =
        batchScheduleRepository.claim(scheduleId, previous, next, now) == 1

}
