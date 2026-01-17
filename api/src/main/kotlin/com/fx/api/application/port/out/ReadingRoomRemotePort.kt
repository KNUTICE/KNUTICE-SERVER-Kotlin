package com.fx.api.application.port.out

import com.fx.api.domain.ReadingRoomSeat
import com.fx.api.domain.ReadingRoomStatus

interface ReadingRoomRemotePort {

    suspend fun getCsrfToken(): String

    suspend fun getReadingRoomStatus(): List<ReadingRoomStatus>

    suspend fun getReadingRoomSeats(roomId: Int, csrfToken: String): List<ReadingRoomSeat>

}