package com.fx.api.application.service.readingroom

import com.fx.api.application.port.`in`.readingroom.ReadingRoomQueryUseCase
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.api.application.port.out.ReadingRoomRemotePort
import com.fx.api.application.port.out.readingroom.SeatAlertPersistencePort
import com.fx.api.domain.ReadingRoomSeat
import com.fx.api.domain.ReadingRoomStatus
import com.fx.global.domain.readingroom.ReadingRoom
import com.fx.global.domain.readingroom.SeatAlert
import com.fx.global.exception.FcmTokenException
import com.fx.global.exception.errorcode.FcmTokenErrorCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ReadingRoomQueryService(
    private val readingRoomRemotePort: ReadingRoomRemotePort,
    private val fcmTokenPersistencePort: FcmTokenPersistencePort,
    private val seatAlertPersistencePort: SeatAlertPersistencePort
) : ReadingRoomQueryUseCase {

    private val log = LoggerFactory.getLogger(ReadingRoomQueryService::class.java)

    override suspend fun getReadingRoomStatus(fcmToken: String): List<ReadingRoomStatus> {
        validateFcmToken(fcmToken)
        return readingRoomRemotePort.getReadingRoomStatus()
    }

    override suspend fun getReadingRoomSeats(fcmToken: String, readingRoom: ReadingRoom): List<ReadingRoomSeat> {
        validateFcmToken(fcmToken)
        return readingRoomRemotePort.getReadingRoomSeats(readingRoom, readingRoomRemotePort.getCsrfToken())
    }

    override fun getSeatAlerts(fcmToken: String): List<SeatAlert> {
        validateFcmToken(fcmToken)
        return seatAlertPersistencePort.findByFcmTokenAndStatus(fcmToken, SeatAlert.SeatAlertStatus.ACTIVE)
    }

    private fun validateFcmToken(fcmToken: String) {
        if (!fcmTokenPersistencePort.existsByFcmToken(fcmToken)) {
            throw FcmTokenException(FcmTokenErrorCode.TOKEN_NOT_FOUND)
        }
    }

}