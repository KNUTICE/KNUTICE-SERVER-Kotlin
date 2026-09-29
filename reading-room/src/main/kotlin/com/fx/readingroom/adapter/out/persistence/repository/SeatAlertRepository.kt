package com.fx.readingroom.adapter.out.persistence.repository

import com.fx.readingroom.domain.ReadingRoom
import com.fx.readingroom.domain.SeatAlert
import com.fx.readingroom.domain.SeatAlertTarget
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface SeatAlertRepository : JpaRepository<SeatAlert, Long> {

    fun findAllByFcmTokenIdAndExpiresAtAfterOrderByIdDesc(fcmTokenId: Long, now: LocalDateTime): List<SeatAlert>

    @Modifying
    @Query("DELETE FROM SeatAlert s WHERE s.id = :id AND s.fcmTokenId = :fcmTokenId")
    fun deleteByIdAndFcmTokenId(id: Long, fcmTokenId: Long): Int

    @Modifying
    @Query(
        """
        DELETE FROM SeatAlert s
        WHERE s.fcmTokenId = :fcmTokenId AND s.readingRoom = :readingRoom AND s.seatNumber = :seatNumber
          AND s.expiresAt <= :now
        """
    )
    fun deleteExpiredSeat(fcmTokenId: Long, readingRoom: ReadingRoom, seatNumber: Int, now: LocalDateTime): Int

    /** `idx_seat_alert_expires_at` 로 만료분만 지운다. */
    @Modifying
    @Query("DELETE FROM SeatAlert s WHERE s.expiresAt <= :now")
    fun deleteExpired(now: LocalDateTime): Int

    @Query(
        """
        SELECT new com.fx.readingroom.domain.SeatAlertTarget(s.id, s.fcmTokenId, t.token, t.language, s.readingRoom, s.seatNumber)
        FROM SeatAlert s JOIN FcmToken t ON t.id = s.fcmTokenId
        WHERE s.expiresAt > :now
        ORDER BY s.id
        """
    )
    fun findActiveTargets(now: LocalDateTime): List<SeatAlertTarget>

}
