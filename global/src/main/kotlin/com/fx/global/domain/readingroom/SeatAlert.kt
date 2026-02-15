package com.fx.global.domain.readingroom

import java.time.LocalDateTime
data class SeatAlert(

    val id: String?,
    val fcmToken: String,
    val readingRoom: ReadingRoom,
    val seatNumber: String,
    val status: SeatAlertStatus,
    val notifiedAt: LocalDateTime? = null,
    val expiresAt: LocalDateTime,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null

) {

    enum class SeatAlertStatus {
        ACTIVE,
        TRIGGERED,
        CANCELLED
    }

    enum class ReadingRoom(val displayName: String) {
        ROOM1("제1집중 학습 ZONE (콘센트)"),
        ROOM2("제2집중 학습 ZONE"),
        ROOM3("제3협업 학습 ZONE (콘센트)")
    }

}