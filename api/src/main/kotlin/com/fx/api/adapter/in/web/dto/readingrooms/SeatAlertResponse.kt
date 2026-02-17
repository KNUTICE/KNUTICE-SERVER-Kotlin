package com.fx.api.adapter.`in`.web.dto.readingrooms

import com.fx.readingroom.domain.ReadingRoom
import com.fx.readingroom.domain.SeatAlert
import com.fx.readingroom.domain.SeatAlert.SeatAlertStatus
import java.time.LocalDateTime

/**
 * 열람실 좌석 알림 조회 DTO
 *
 * @author 이동섭
 * @since 2026-02-15
 */
data class SeatAlertResponse(

    val alertId: String, // 알림 ID
    val readingRoom: ReadingRoom,
    val seatNumber: Int, // 좌석 번호
    val status: SeatAlertStatus, // 알림 상태
    val createdAt: LocalDateTime // 알림 생성 시간

) {
    companion object {
        fun from(seatAlert: SeatAlert): SeatAlertResponse =
            SeatAlertResponse(
                alertId = seatAlert.id!!,
                readingRoom = seatAlert.readingRoom,
                seatNumber = seatAlert.seatNumber,
                status = seatAlert.status,
                createdAt = seatAlert.createdAt!!
            )

        fun from(seatAlerts: List<SeatAlert>): List<SeatAlertResponse> =
            seatAlerts.map { from(it) }
    }

}