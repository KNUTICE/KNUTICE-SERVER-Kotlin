package com.fx.common.domain.batch

import com.fx.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

/** 거절 사유 최대 길이 — `reject_reason` 컬럼 길이와 같아야 한다. */
const val BATCH_REJECT_REASON_MAX_LENGTH = 500

/**
 * 관리자의 수동 실행 요청. 관리자 API 가 저장하고 crawler 폴러가 다음 확인 때(최대 1분 뒤) 실행한다.
 * 요청 시각은 `created_at` 이다.
 */
@Entity
@Table(
    name = "batch_run_request",
    indexes = [
        // 처리 대상 : `WHERE status = 'REQUESTED' ORDER BY id`
        Index(name = "idx_batch_run_request_status_id", columnList = "status, id"),
    ],
)
class BatchRunRequest(
    jobName: String,
    jobParameters: String,
    requestedBy: String,
) : BaseEntity() {

    @Column(name = "job_name", nullable = false, updatable = false, length = BATCH_JOB_NAME_MAX_LENGTH, comment = "실행할 Job 이름")
    val jobName: String = jobName

    @Column(name = "job_parameters", nullable = false, updatable = false, length = BATCH_JOB_PARAMETERS_MAX_LENGTH, comment = "Job 파라미터 (JSON 객체)")
    val jobParameters: String = jobParameters

    @Column(name = "requested_by", nullable = false, updatable = false, length = 100, comment = "요청한 관리자")
    val requestedBy: String = requestedBy

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, comment = "REQUESTED / LAUNCHED / REJECTED")
    var status: BatchRunRequestStatus = BatchRunRequestStatus.REQUESTED
        protected set

    @Column(name = "job_execution_id", nullable = true, comment = "실행한 BATCH_JOB_EXECUTION ID")
    var jobExecutionId: Long? = null
        protected set

    @Column(name = "processed_at", nullable = true, comment = "실행 · 거절 시각")
    var processedAt: LocalDateTime? = null
        protected set

    @Column(name = "reject_reason", nullable = true, length = BATCH_REJECT_REASON_MAX_LENGTH, comment = "거절 사유")
    var rejectReason: String? = null
        protected set

}
