package com.fx.api.application.port.`in`.batch

import com.fx.api.application.port.`in`.batch.dto.BatchRunRequestCommand
import com.fx.common.domain.batch.BatchRunRequest

interface BatchRunRequestCommandUseCase {

    fun requestRun(command: BatchRunRequestCommand): BatchRunRequest

}
