package com.fx.api.application.port.`in`.batch

import com.fx.api.application.port.`in`.batch.dto.BatchScheduleCreateCommand
import com.fx.api.application.port.`in`.batch.dto.BatchScheduleUpdateCommand
import com.fx.api.domain.BatchScheduleDetail

interface BatchScheduleCommandUseCase {

    fun createSchedule(command: BatchScheduleCreateCommand): BatchScheduleDetail

    fun updateSchedule(command: BatchScheduleUpdateCommand): BatchScheduleDetail

    fun changeEnabled(scheduleKey: String, enabled: Boolean): BatchScheduleDetail

}
