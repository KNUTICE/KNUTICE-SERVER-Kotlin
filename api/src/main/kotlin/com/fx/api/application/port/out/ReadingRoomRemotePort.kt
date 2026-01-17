package com.fx.api.application.port.out

import com.fx.api.domain.ReadingRoom

interface ReadingRoomRemotePort {

    suspend fun getCsrfToken(): String

    suspend fun getReadingRooms(roomId: Int, csrfToken: String): List<ReadingRoom>

}