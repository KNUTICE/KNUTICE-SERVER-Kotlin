package com.fx.api.adapter.`in`.web.dto.readingrooms

import com.fx.api.application.port.`in`.dto.CreateSeatAlertCommand
import com.fx.global.domain.readingroom.SeatAlert.ReadingRoom

/**
 * 열람실 좌석 알림 요청 DTO
 *
 * @author 이동섭
 * @since 2026-02-15
 */
data class CreateSeatAlertRequest (

    val readingRoom: ReadingRoom, // 열람실 이름
    val seatNumber: Int, // 좌석 번호

) {
    fun toCommand(fcmToken: String) =
        CreateSeatAlertCommand(
            fcmToken = fcmToken,
            readingRoom = readingRoom,
            seatNumber = seatNumber
        )
}
