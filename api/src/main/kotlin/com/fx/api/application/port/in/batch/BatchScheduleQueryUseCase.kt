package com.fx.api.application.port.`in`.batch

import com.fx.api.domain.BatchScheduleDetail

interface BatchScheduleQueryUseCase {

    /** scheduleKey 순. */
    fun getSchedules(): List<BatchScheduleDetail>

}
