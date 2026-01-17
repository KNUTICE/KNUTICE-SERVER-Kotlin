package com.fx.api.application.service

import com.fx.api.application.port.`in`.ReadingRoomQueryUseCase
import com.fx.api.application.port.out.ReadingRoomRemotePort
import com.fx.api.domain.ReadingRoomSeat
import com.fx.api.domain.ReadingRoomStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ReadingRoomQueryService(
    private val readingRoomRemotePort: ReadingRoomRemotePort
) : ReadingRoomQueryUseCase {

    private val log = LoggerFactory.getLogger(ReadingRoomQueryService::class.java)

    override suspend fun getReadingRoomStatus(fcmToken: String): List<ReadingRoomStatus> =
        readingRoomRemotePort.getReadingRoomStatus(readingRoomRemotePort.getCsrfToken())

    override suspend fun getReadingRoomSeats(fcmToken: String, roomId: Int): List<ReadingRoomSeat> =
        readingRoomRemotePort.getReadingRoomSeats(roomId, readingRoomRemotePort.getCsrfToken())

}