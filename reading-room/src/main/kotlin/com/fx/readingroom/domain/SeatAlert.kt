package com.fx.readingroom.domain
import java.time.LocalDateTime

data class SeatAlert(

    val id: String?,
    val fcmToken: String,
    val readingRoom: ReadingRoom,
    val seatNumber: Int,
    val status: SeatAlertStatus,
    val notifiedAt: LocalDateTime? = null,
    val expiresAt: LocalDateTime,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null

) {

    companion object {
        fun createSeatAlert(fcmToken: String, readingRoom: ReadingRoom, seatNumber: Int): SeatAlert {
            return SeatAlert(
                id = null,
                fcmToken = fcmToken,
                readingRoom = readingRoom,
                seatNumber = seatNumber,
                status = SeatAlertStatus.ACTIVE,
                expiresAt = LocalDateTime.now().plusHours(12)
            )
        }
    }

    // dslee (2026-02-18) : 좌석 알림이 발송되거나 취소 시 바로 DB 삭제를 하므로 현재는 ACTIVE 만 사용함
    enum class SeatAlertStatus {
        ACTIVE,
        TRIGGERED,
        CANCELLED
    }

}