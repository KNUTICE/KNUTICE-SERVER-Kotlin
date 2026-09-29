package com.fx.api.application.service.readingroom

import com.fx.api.application.port.`in`.readingroom.ReadingRoomQueryUseCase
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.common.exception.FcmTokenException
import com.fx.common.exception.errorcode.FcmTokenErrorCode
import com.fx.readingroom.application.port.out.ReadingRoomRemotePort
import com.fx.readingroom.application.port.out.SeatAlertPersistencePort
import com.fx.readingroom.domain.ReadingRoom
import com.fx.readingroom.domain.ReadingRoomSeat
import com.fx.readingroom.domain.ReadingRoomStatus
import com.fx.readingroom.domain.SeatAlert
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDateTime

/** 열람실 사이트 조회를 기다리는 동안 DB 커넥션을 잡지 않도록 서비스에는 트랜잭션을 걸지 않는다. */
@Service
class ReadingRoomQueryService(
    private val readingRoomRemotePort: ReadingRoomRemotePort,
    private val fcmTokenPersistencePort: FcmTokenPersistencePort,
    private val seatAlertPersistencePort: SeatAlertPersistencePort,
    private val clock: Clock,
) : ReadingRoomQueryUseCase {

    override fun getReadingRoomStatus(fcmToken: String): List<ReadingRoomStatus> {
        requireExistingToken(fcmToken)
        return readingRoomRemotePort.getReadingRoomStatus()
    }

    override fun getReadingRoomSeats(fcmToken: String, readingRoom: ReadingRoom): List<ReadingRoomSeat> {
        requireExistingToken(fcmToken)
        return readingRoomRemotePort.getReadingRoomSeats(readingRoom, readingRoomRemotePort.getCsrfToken())
    }

    override fun getSeatAlerts(fcmToken: String): List<SeatAlert> {
        val fcmTokenId = requireNotNull(fcmTokenPersistencePort.getByToken(fcmToken).id)
        return seatAlertPersistencePort.findActiveByFcmTokenId(fcmTokenId, LocalDateTime.now(clock))
    }

    private fun requireExistingToken(fcmToken: String) {
        if (!fcmTokenPersistencePort.existsByToken(fcmToken)) {
            throw FcmTokenException(FcmTokenErrorCode.TOKEN_NOT_FOUND)
        }
    }

}
