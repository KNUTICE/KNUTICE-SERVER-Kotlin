package com.fx.common.adapter.out.persistence.repository

import com.fx.common.domain.batch.BatchSchedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface BatchScheduleRepository : JpaRepository<BatchSchedule, Long> {

    fun findAllByOrderByScheduleKeyAsc(): List<BatchSchedule>

    fun findByScheduleKey(scheduleKey: String): BatchSchedule?

    fun existsByScheduleKey(scheduleKey: String): Boolean

    fun findAllByEnabledTrueAndNextFireAtIsNull(): List<BatchSchedule>

    fun findAllByEnabledTrueAndNextFireAtLessThanEqualOrderByNextFireAtAsc(now: LocalDateTime): List<BatchSchedule>

    /** 아직 계산하지 않은 다음 발화 시각을 채운다. 다른 인스턴스가 먼저 채웠으면 0. */
    @Modifying
    @Query(
        """
        UPDATE BatchSchedule s SET s.nextFireAt = :next, s.updatedAt = :now
        WHERE s.id = :id AND s.enabled = true AND s.nextFireAt IS NULL
        """
    )
    fun initializeNextFireAt(id: Long, next: LocalDateTime, now: LocalDateTime): Int

    /**
     * 발화를 선점한다. 읽은 뒤 [previous] 가 그대로일 때만 다음 발화 시각으로 넘기므로
     * 여러 인스턴스가 같은 발화를 보더라도 한 곳만 1 을 받는다.
     */
    @Modifying
    @Query(
        """
        UPDATE BatchSchedule s SET s.nextFireAt = :next, s.lastFiredAt = :now, s.updatedAt = :now
        WHERE s.id = :id AND s.enabled = true AND s.nextFireAt = :previous
        """
    )
    fun claim(id: Long, previous: LocalDateTime, next: LocalDateTime, now: LocalDateTime): Int

}
