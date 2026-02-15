package com.fx.api.application.service.readingroom

import com.fx.api.application.port.`in`.dto.CreateSeatAlertCommand
import com.fx.api.application.port.`in`.readingroom.ReadingRoomCommandUseCase
import com.fx.api.application.port.`in`.readingroom.ReadingRoomQueryUseCase
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.api.application.port.out.ReadingRoomRemotePort
import com.fx.api.domain.ReadingRoomSeat
import com.fx.api.domain.ReadingRoomStatus
import com.fx.global.exception.FcmTokenException
import com.fx.global.exception.errorcode.FcmTokenErrorCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ReadingRoomCommandService(
    private val fcmTokenPersistencePort: FcmTokenPersistencePort,
    private val readingRoomRemotePort: ReadingRoomRemotePort,
    private val seatAlertPersistencePort: SeatAlertPersistencePort
) : ReadingRoomCommandUseCase {

    private val log = LoggerFactory.getLogger(ReadingRoomCommandService::class.java)

    override suspend fun createSeatAlert(seatAlertCommand: CreateSeatAlertCommand): String {
        TODO("Not yet implemented")
    }

}