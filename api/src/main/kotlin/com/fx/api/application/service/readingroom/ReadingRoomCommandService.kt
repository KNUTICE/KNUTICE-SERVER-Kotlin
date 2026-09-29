package com.fx.api.application.service.readingroom

import com.fx.api.application.port.`in`.dto.CreateSeatAlertCommand
import com.fx.api.application.port.`in`.readingroom.ReadingRoomCommandUseCase
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.readingroom.application.port.out.ReadingRoomRemotePort
import com.fx.readingroom.application.port.out.SeatAlertPersistencePort
import com.fx.readingroom.domain.SeatAlert
import com.fx.readingroom.domain.exception.DuplicateSeatAlertException
import com.fx.readingroom.exception.ReadingRoomException
import com.fx.readingroom.exception.errorcode.ReadingRoomErrorCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDateTime

/**
 * 좌석 알림 등록 · 삭제.
 * 열람실 사이트 조회를 기다리는 동안 DB 커넥션을 잡지 않도록 서비스에는 트랜잭션을 걸지 않는다.
 */
@Service
class ReadingRoomCommandService(
    private val fcmTokenPersistencePort: FcmTokenPersistencePort,
    private val readingRoomRemotePort: ReadingRoomRemotePort,
    private val seatAlertPersistencePort: SeatAlertPersistencePort,
    private val clock: Clock,
) : ReadingRoomCommandUseCase {

    private val log = LoggerFactory.getLogger(ReadingRoomCommandService::class.java)

    override fun createSeatAlert(seatAlertCommand: CreateSeatAlertCommand): SeatAlert {
        val fcmTokenId = requireNotNull(fcmTokenPersistencePort.getByToken(seatAlertCommand.fcmToken).id)

        val seat = readingRoomRemotePort.getReadingRoomSeats(
            seatAlertCommand.readingRoom,
            readingRoomRemotePort.getCsrfToken(),
        ).find { it.seatNumber == seatAlertCommand.seatNumber }
            ?: throw ReadingRoomException(ReadingRoomErrorCode.SEAT_NOT_FOUND)

        // 이미 비어 있는 좌석에는 알림을 걸지 않는다
        if (seat.isAvailable) {
            throw ReadingRoomException(ReadingRoomErrorCode.SEAT_ALREADY_AVAILABLE)
        }

        val now = LocalDateTime.now(clock)
        val activeAlerts = seatAlertPersistencePort.findActiveByFcmTokenId(fcmTokenId, now)
        if (activeAlerts.size >= SeatAlert.MAX_ACTIVE_PER_TOKEN) {
            throw ReadingRoomException(ReadingRoomErrorCode.MAX_SEAT_ALERT_LIMIT_EXCEEDED)
        }
        if (activeAlerts.any { it.isSameSeat(seatAlertCommand.readingRoom, seatAlertCommand.seatNumber) }) {
            throw ReadingRoomException(ReadingRoomErrorCode.SEAT_ALERT_ALREADY_EXISTS)
        }

        return try {
            seatAlertPersistencePort.create(
                SeatAlert.create(fcmTokenId, seatAlertCommand.readingRoom, seatAlertCommand.seatNumber, now)
            )
        } catch (e: DuplicateSeatAlertException) {
            log.warn("중복 알림 등록 시도: {}", seatAlertCommand, e)
            throw ReadingRoomException(ReadingRoomErrorCode.SEAT_ALERT_ALREADY_EXISTS)
        }
    }

    override fun deleteSeatAlert(fcmToken: String, alertId: String): Boolean {
        val fcmTokenId = requireNotNull(fcmTokenPersistencePort.getByToken(fcmToken).id)
        val seatAlertId = alertId.toLongOrNull()
            ?: throw ReadingRoomException(ReadingRoomErrorCode.SEAT_NOT_FOUND)

        if (!seatAlertPersistencePort.deleteByIdAndFcmTokenId(seatAlertId, fcmTokenId)) {
            throw ReadingRoomException(ReadingRoomErrorCode.SEAT_NOT_FOUND)
        }
        return true
    }

}
