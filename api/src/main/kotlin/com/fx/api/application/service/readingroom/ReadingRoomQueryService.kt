package com.fx.api.application.service.readingroom

import com.fx.api.application.port.`in`.readingroom.ReadingRoomQueryUseCase
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.api.application.port.out.ReadingRoomRemotePort
import com.fx.api.domain.ReadingRoomSeat
import com.fx.api.domain.ReadingRoomStatus
import com.fx.global.domain.readingroom.ReadingRoom
import com.fx.global.exception.FcmTokenException
import com.fx.global.exception.errorcode.FcmTokenErrorCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ReadingRoomQueryService(
    private val readingRoomRemotePort: ReadingRoomRemotePort,
    private val fcmTokenPersistencePort: FcmTokenPersistencePort
) : ReadingRoomQueryUseCase {

    private val log = LoggerFactory.getLogger(ReadingRoomQueryService::class.java)

    override suspend fun getReadingRoomStatus(fcmToken: String): List<ReadingRoomStatus> {
        fcmTokenPersistencePort.findByFcmToken(fcmToken)
            ?: throw FcmTokenException(FcmTokenErrorCode.TOKEN_NOT_FOUND)

        return readingRoomRemotePort.getReadingRoomStatus()
    }
    override suspend fun getReadingRoomSeats(fcmToken: String, readingRoom: ReadingRoom): List<ReadingRoomSeat> {
        fcmTokenPersistencePort.findByFcmToken(fcmToken)
            ?: throw FcmTokenException(FcmTokenErrorCode.TOKEN_NOT_FOUND)

        return readingRoomRemotePort.getReadingRoomSeats(readingRoom, readingRoomRemotePort.getCsrfToken())
    }

}