package com.fx.global.domain.readingroom
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

    enum class SeatAlertStatus {
        ACTIVE,
        TRIGGERED,
        CANCELLED
    }

}