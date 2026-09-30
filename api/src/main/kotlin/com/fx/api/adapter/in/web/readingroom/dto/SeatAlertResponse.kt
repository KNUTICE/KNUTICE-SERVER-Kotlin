package com.fx.api.adapter.`in`.web.readingroom.dto

import com.fx.readingroom.domain.ReadingRoom
import com.fx.readingroom.domain.SeatAlert
import java.time.LocalDateTime

/**
 * 열람실 좌석 알림 조회 DTO
 *
 * @author 이동섭
 * @since 2026-02-15
 */
data class SeatAlertResponse(
    /** TSID 를 문자열로 내보낸다 (JS 숫자 정밀도 한계). */
    val alertId: String,
    val readingRoom: ReadingRoom,
    val seatNumber: Int,
    /** 조회되는 알림은 모두 활성 알림이다. 응답 호환을 위해 `ACTIVE` 를 그대로 내보낸다. */
    val status: String,
    val createdAt: LocalDateTime
) {

    companion object {

        private const val ACTIVE = "ACTIVE"

        fun from(seatAlert: SeatAlert): SeatAlertResponse =
            SeatAlertResponse(
                alertId = requireNotNull(seatAlert.id).toString(),
                readingRoom = seatAlert.readingRoom,
                seatNumber = seatAlert.seatNumber,
                status = ACTIVE,
                createdAt = requireNotNull(seatAlert.createdAt)
            )

        fun from(seatAlerts: List<SeatAlert>): List<SeatAlertResponse> =
            seatAlerts.map { from(it) }
    }

}
