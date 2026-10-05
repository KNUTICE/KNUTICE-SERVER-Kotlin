package com.fx.api.application.port.`in`.readingroom

import com.fx.readingroom.domain.ReadingRoom
import com.fx.readingroom.domain.ReadingRoomSeat
import com.fx.readingroom.domain.ReadingRoomStatus
import com.fx.readingroom.domain.SeatAlert

interface ReadingRoomQueryUseCase {

    fun getReadingRoomStatus(fcmToken: String): List<ReadingRoomStatus>

    fun getReadingRoomSeats(fcmToken: String, readingRoom: ReadingRoom): List<ReadingRoomSeat>

    /** 토큰의 만료되지 않은 좌석 알림. 최근 등록 순. */
    fun getSeatAlerts(fcmToken: String): List<SeatAlert>

}
