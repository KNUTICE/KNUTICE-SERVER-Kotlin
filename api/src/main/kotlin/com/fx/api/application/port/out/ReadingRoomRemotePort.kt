package com.fx.api.application.port.out

import com.fx.api.domain.ReadingRoomSeat
import com.fx.api.domain.ReadingRoomStatus
import com.fx.global.domain.readingroom.ReadingRoom

interface ReadingRoomRemotePort {

    suspend fun getCsrfToken(): String

    suspend fun getReadingRoomStatus(): List<ReadingRoomStatus>

    suspend fun getReadingRoomSeats(readingRoom: ReadingRoom, csrfToken: String): List<ReadingRoomSeat>

}