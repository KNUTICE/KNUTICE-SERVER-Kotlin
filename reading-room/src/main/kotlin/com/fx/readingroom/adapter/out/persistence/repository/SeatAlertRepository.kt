package com.fx.readingroom.adapter.out.persistence.repository

import com.fx.readingroom.domain.ReadingRoom
import com.fx.readingroom.domain.SeatAlert
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

}
