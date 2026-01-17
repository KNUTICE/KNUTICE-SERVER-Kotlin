package com.fx.api.adapter.out.web

import com.fx.api.application.port.out.ReadingRoomRemotePort
import com.fx.api.domain.ReadingRoom
import com.fx.global.annotation.hexagonal.WebOutputAdapter

@WebOutputAdapter
class ReadingRoomRemoteAdapter() : ReadingRoomRemotePort {

    override suspend fun getReadingRooms(roomId: Int): List<ReadingRoom> {
        TODO("Not yet implemented")
    }

}