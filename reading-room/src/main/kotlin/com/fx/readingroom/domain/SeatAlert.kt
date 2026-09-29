package com.fx.readingroom.domain

import com.fx.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Duration
import java.time.LocalDateTime

/**
 * 열람실 빈자리 알림.
 *
 * 행이 있으면 활성 알림이다. 알림을 보냈거나 사용자가 취소하면 바로 삭제하고,
 * 끝까지 자리가 나지 않은 알림은 [expiresAt] 이 지나면 빈자리 체크 배치가 지운다.
 * 만료 전이라도 [isExpired] 인 알림은 조회 · 발송 대상에서 뺀다.
 */
@Entity
@Table(
    name = "seat_alert",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_seat_alert_fcm_token_id_reading_room_seat_number", columnNames = ["fcm_token_id", "reading_room", "seat_number"]),
    ],
    indexes = [
        // 만료 정리 : `WHERE expires_at <= ?`
        Index(name = "idx_seat_alert_expires_at", columnList = "expires_at"),
    ],
)
class SeatAlert(
    fcmTokenId: Long,
    readingRoom: ReadingRoom,
    seatNumber: Int,
    expiresAt: LocalDateTime,
) : BaseEntity() {

    @Column(name = "fcm_token_id", nullable = false, updatable = false, comment = "FCM 토큰 ID")
    val fcmTokenId: Long = fcmTokenId

    @Enumerated(EnumType.STRING)
    @Column(name = "reading_room", nullable = false, updatable = false, length = 20, comment = "열람실")
    val readingRoom: ReadingRoom = readingRoom

    @Column(name = "seat_number", nullable = false, updatable = false, comment = "좌석 번호")
    val seatNumber: Int = seatNumber

    @Column(name = "expires_at", nullable = false, updatable = false, comment = "만료 시각")
    val expiresAt: LocalDateTime = expiresAt

    fun isExpired(now: LocalDateTime): Boolean = !now.isBefore(expiresAt)

    fun isSameSeat(readingRoom: ReadingRoom, seatNumber: Int): Boolean =
        this.readingRoom == readingRoom && this.seatNumber == seatNumber

    companion object {

        /** 알림 유효 시간. 지나면 자리가 나지 않았어도 알림을 끝낸다. */
        val LIFETIME: Duration = Duration.ofHours(12)

        /** 한 토큰이 동시에 걸어 둘 수 있는 알림 수. */
        const val MAX_ACTIVE_PER_TOKEN = 5

        fun create(fcmTokenId: Long, readingRoom: ReadingRoom, seatNumber: Int, now: LocalDateTime): SeatAlert =
            SeatAlert(fcmTokenId, readingRoom, seatNumber, expiresAt = now.plus(LIFETIME))

    }

}
