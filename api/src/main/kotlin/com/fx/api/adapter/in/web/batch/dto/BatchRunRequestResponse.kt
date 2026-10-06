package com.fx.api.adapter.`in`.web.batch.dto

import com.fx.common.domain.batch.BatchJobParameters
import com.fx.common.domain.batch.BatchRunRequest
import com.fx.common.domain.batch.BatchRunRequestStatus
import java.time.LocalDateTime

data class BatchRunRequestResponse(
    /** TSID 를 문자열로 내보낸다 (JS 숫자 정밀도 한계). */
    val id: String,
    val jobName: String,
    val jobParameters: Map<String, String>,
    val requestedBy: String,
    val status: BatchRunRequestStatus,
    /** 실행했으면 그 실행의 ID (실행 이력의 id) */
    val jobExecutionId: Long?,
    /** 거절했으면 그 이유 */
    val rejectReason: String?,
    val requestedAt: LocalDateTime,
    /** 실행 · 거절한 시각. 아직 대기 중이면 null */
    val processedAt: LocalDateTime?,
) {

    companion object {

        fun from(request: BatchRunRequest): BatchRunRequestResponse =
            BatchRunRequestResponse(
                id = requireNotNull(request.id).toString(),
                jobName = request.jobName,
                jobParameters = BatchJobParameters.parse(request.jobParameters),
                requestedBy = request.requestedBy,
                status = request.status,
                jobExecutionId = request.jobExecutionId,
                rejectReason = request.rejectReason,
                requestedAt = requireNotNull(request.createdAt),
                processedAt = request.processedAt,
            )

    }

}
