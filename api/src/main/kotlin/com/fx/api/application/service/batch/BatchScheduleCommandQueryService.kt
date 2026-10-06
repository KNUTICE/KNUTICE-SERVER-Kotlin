package com.fx.api.application.service.batch

import com.fx.api.application.port.`in`.batch.BatchScheduleCommandUseCase
import com.fx.api.application.port.`in`.batch.BatchScheduleQueryUseCase
import com.fx.api.application.port.`in`.batch.dto.BatchScheduleCreateCommand
import com.fx.api.application.port.`in`.batch.dto.BatchScheduleUpdateCommand
import com.fx.api.application.port.out.batch.BatchExecutionPersistencePort
import com.fx.api.application.port.out.batch.BatchSchedulePersistencePort
import com.fx.api.domain.BatchScheduleDetail
import com.fx.api.exception.BatchException
import com.fx.api.exception.errorcode.BatchErrorCode
import com.fx.common.domain.batch.BatchSchedule
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

/**
 * 배치 스케줄 조회 · 추가 · 수정 · 켜기 · 끄기.
 *
 * crawler 의 폴러가 매분 스케줄 테이블을 읽으므로 저장만 하면 다음 확인부터 반영된다.
 * cron 을 바꾸거나 켜면 다음 실행 시각을 바로 다시 계산해, 옛 시각에 한 번 더 실행되지 않게 한다.
 */
@Service
@Transactional(readOnly = true)
class BatchScheduleCommandQueryService(
    private val batchSchedulePersistencePort: BatchSchedulePersistencePort,
    private val batchExecutionPersistencePort: BatchExecutionPersistencePort,
    private val clock: Clock,
) : BatchScheduleCommandUseCase, BatchScheduleQueryUseCase {

    override fun getSchedules(): List<BatchScheduleDetail> {
        val schedules = batchSchedulePersistencePort.findAll()
        val scheduleKeys = schedules.map {
            it.scheduleKey
        }
        val lastExecutions = batchExecutionPersistencePort.findLastExecutions(scheduleKeys)
        return schedules.map {
            BatchScheduleDetail(it, lastExecutions[it.scheduleKey])
        }
    }

    @Transactional
    override fun createSchedule(command: BatchScheduleCreateCommand): BatchScheduleDetail {
        val now = LocalDateTime.now(clock)
        val jobParameters = BatchInputValidator.jobParameters(command.jobName, command.jobParameters)
        val cron = BatchInputValidator.cron(command.cron, now)
        if (batchSchedulePersistencePort.existsByScheduleKey(command.scheduleKey)) {
            throw BatchException(BatchErrorCode.SCHEDULE_KEY_DUPLICATED)
        }

        val schedule = BatchSchedule(
            scheduleKey = command.scheduleKey,
            jobName = command.jobName,
            jobParameters = jobParameters,
            cron = cron,
            enabled = false,
            description = command.description,
        )
        // 생성자로 켜면 다음 실행 시각이 비어 폴러가 채울 때까지 1분을 더 기다린다
        if (command.enabled) {
            schedule.enable(now)
        }
        return detail(batchSchedulePersistencePort.create(schedule))
    }

    @Transactional
    override fun updateSchedule(command: BatchScheduleUpdateCommand): BatchScheduleDetail {
        val now = LocalDateTime.now(clock)
        val schedule = batchSchedulePersistencePort.getByScheduleKey(command.scheduleKey)

        command.cron?.let {
            schedule.changeCron(BatchInputValidator.cron(it, now), now)
        }
        command.jobParameters?.let {
            schedule.changeJobParameters(BatchInputValidator.jobParameters(schedule.jobName, it))
        }
        command.description?.let {
            schedule.changeDescription(it)
        }
        return detail(schedule)
    }

    /** 켜면 지금 이후 첫 시각부터 다시 센다. 꺼져 있던 동안 지난 실행은 하지 않는다. */
    @Transactional
    override fun changeEnabled(scheduleKey: String, enabled: Boolean): BatchScheduleDetail {
        val schedule = batchSchedulePersistencePort.getByScheduleKey(scheduleKey)
        if (enabled) {
            schedule.enable(LocalDateTime.now(clock))
        } else {
            schedule.disable()
        }
        return detail(schedule)
    }

    private fun detail(schedule: BatchSchedule): BatchScheduleDetail =
        BatchScheduleDetail(
            schedule = schedule,
            lastExecution = batchExecutionPersistencePort.findLastExecutions(listOf(schedule.scheduleKey))[schedule.scheduleKey],
        )

}
