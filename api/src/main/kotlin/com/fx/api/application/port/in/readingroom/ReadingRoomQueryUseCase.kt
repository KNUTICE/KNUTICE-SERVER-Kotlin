package com.fx.api.application.port.`in`.readingroom

import com.fx.readingroom.domain.ReadingRoomSeat
import com.fx.readingroom.domain.ReadingRoomStatus
import com.fx.readingroom.domain.ReadingRoom
import com.fx.readingroom.domain.SeatAlert

/**
 * 열람실 관련 조회를 위한 Input Port 입니다.
 *
 * @author 이동섭
 * @since 2026-01-17
 */
interface ReadingRoomQueryUseCase {

    suspend fun getReadingRoomStatus(fcmToken: String): List<ReadingRoomStatus>

    suspend fun getReadingRoomSeats(fcmToken: String, readingRoom: ReadingRoom): List<ReadingRoomSeat>

    fun getSeatAlerts(fcmToken: String): List<SeatAlert>

}