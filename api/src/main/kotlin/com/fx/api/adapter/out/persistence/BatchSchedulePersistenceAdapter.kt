package com.fx.api.adapter.out.persistence

import com.fx.api.application.port.out.batch.BatchSchedulePersistencePort
import com.fx.api.exception.BatchException
import com.fx.api.exception.errorcode.BatchErrorCode
import com.fx.common.adapter.out.persistence.repository.BatchScheduleRepository
import com.fx.common.annotation.PersistenceAdapter
import com.fx.common.domain.batch.BatchSchedule
import org.springframework.dao.DataIntegrityViolationException

@PersistenceAdapter
class BatchSchedulePersistenceAdapter(
    private val batchScheduleRepository: BatchScheduleRepository,
) : BatchSchedulePersistencePort {

    override fun findAll(): List<BatchSchedule> =
        batchScheduleRepository.findAllByOrderByScheduleKeyAsc()

    override fun getByScheduleKey(scheduleKey: String): BatchSchedule =
        batchScheduleRepository.findByScheduleKey(scheduleKey)
            ?: throw BatchException(BatchErrorCode.SCHEDULE_NOT_FOUND)

    override fun existsByScheduleKey(scheduleKey: String): Boolean =
        batchScheduleRepository.existsByScheduleKey(scheduleKey)

    /** 중복 확인과 저장 사이에 같은 키가 먼저 저장되면 유니크 제약이 막는다. */
    override fun create(schedule: BatchSchedule): BatchSchedule =
        try {
            batchScheduleRepository.saveAndFlush(schedule)
        } catch (e: DataIntegrityViolationException) {
            throw BatchException(BatchErrorCode.SCHEDULE_KEY_DUPLICATED, cause = e)
        }

}
