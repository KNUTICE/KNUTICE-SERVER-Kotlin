package com.fx.readingroom.application.port.out

import com.fx.readingroom.domain.ReadingRoom
import com.fx.readingroom.domain.ReadingRoomSeat
import com.fx.readingroom.domain.ReadingRoomStatus

interface ReadingRoomRemotePort {

    suspend fun getCsrfToken(): String

    suspend fun getReadingRoomStatus(): List<ReadingRoomStatus>

    suspend fun getReadingRoomSeats(readingRoom: ReadingRoom, csrfToken: String): List<ReadingRoomSeat>

}