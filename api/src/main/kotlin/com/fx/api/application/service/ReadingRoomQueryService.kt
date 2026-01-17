package com.fx.api.application.service

import com.fx.api.application.port.`in`.ReadingRoomQueryUseCase
import com.fx.api.application.port.out.ReadingRoomRemotePort
import com.fx.api.domain.ReadingRoom
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ReadingRoomQueryService(
    private val readingRoomRemotePort: ReadingRoomRemotePort
) : ReadingRoomQueryUseCase {

    private val log = LoggerFactory.getLogger(ReadingRoomQueryService::class.java)

    override suspend fun getReadingRooms(fcmToken: String, roomId: Int): List<ReadingRoom> {
        val csrfToken = readingRoomRemotePort.getCsrfToken()
        return readingRoomRemotePort.getReadingRooms(roomId, csrfToken)
    }

}