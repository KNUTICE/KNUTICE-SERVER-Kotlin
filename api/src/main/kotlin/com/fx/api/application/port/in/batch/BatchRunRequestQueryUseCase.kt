package com.fx.api.application.port.`in`.batch

import com.fx.api.application.port.`in`.batch.dto.BatchRunRequestSearchCommand
import com.fx.api.domain.CursorPage
import com.fx.common.domain.batch.BatchRunRequest

interface BatchRunRequestQueryUseCase {

    /** 최신순 */
    fun getRunRequests(command: BatchRunRequestSearchCommand): CursorPage<BatchRunRequest>

}
