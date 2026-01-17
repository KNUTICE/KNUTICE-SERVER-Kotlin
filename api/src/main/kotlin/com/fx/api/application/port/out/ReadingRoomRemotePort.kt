package com.fx.api.application.port.out

import com.fx.api.domain.ReadingRoom

interface ReadingRoomRemotePort {

    suspend fun getReadingRooms(roomId: Int): List<ReadingRoom>

}