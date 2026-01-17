package com.fx.api.application.service

import com.fx.api.application.port.`in`.ReadingRoomQueryUseCase
import com.fx.api.application.port.out.ReadingRoomRemotePort
import com.fx.api.domain.ReadingRoom
import org.springframework.stereotype.Service

@Service
class ReadingRoomQueryService(
    private val readingRoomRemotePort: ReadingRoomRemotePort
) : ReadingRoomQueryUseCase {

    override suspend fun getReadingRooms(fcmToken: String, roomId: Int): List<ReadingRoom> {
        return readingRoomRemotePort.getReadingRooms(roomId)
    }

}