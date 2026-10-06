package com.fx.common.adapter.out.persistence.repository

import com.fx.common.domain.batch.BatchRunRequest
import com.fx.common.domain.batch.BatchRunRequestStatus
import org.springframework.data.domain.Limit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface BatchRunRequestRepository : JpaRepository<BatchRunRequest, Long> {

    fun findAllByStatusOrderByIdAsc(status: BatchRunRequestStatus, limit: Limit): List<BatchRunRequest>

    fun findAllByStatusAndJobName(status: BatchRunRequestStatus, jobName: String): List<BatchRunRequest>

    /** 요청을 선점해 실행 상태로 바꾼다. 다른 인스턴스가 먼저 가져갔으면 0. */
    @Modifying
    @Query(
        """
        UPDATE BatchRunRequest r
        SET r.status = com.fx.common.domain.batch.BatchRunRequestStatus.LAUNCHED, r.processedAt = :now, r.updatedAt = :now
        WHERE r.id = :id AND r.status = com.fx.common.domain.batch.BatchRunRequestStatus.REQUESTED
        """
    )
    fun claim(id: Long, now: LocalDateTime): Int

    @Modifying
    @Query("UPDATE BatchRunRequest r SET r.jobExecutionId = :jobExecutionId, r.updatedAt = :now WHERE r.id = :id")
    fun updateJobExecutionId(id: Long, jobExecutionId: Long, now: LocalDateTime): Int

    @Modifying
    @Query(
        """
        UPDATE BatchRunRequest r
        SET r.status = com.fx.common.domain.batch.BatchRunRequestStatus.REJECTED, r.rejectReason = :reason,
            r.processedAt = :now, r.updatedAt = :now
        WHERE r.id = :id
        """
    )
    fun reject(id: Long, reason: String, now: LocalDateTime): Int

}
