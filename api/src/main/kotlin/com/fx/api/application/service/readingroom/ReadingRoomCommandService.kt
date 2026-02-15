package com.fx.api.application.service.readingroom

import com.fx.api.application.port.`in`.dto.CreateSeatAlertCommand
import com.fx.api.application.port.`in`.readingroom.ReadingRoomCommandUseCase
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.api.application.port.out.ReadingRoomRemotePort
import com.fx.api.application.port.out.readingroom.SeatAlertPersistencePort
import com.fx.global.domain.readingroom.SeatAlert
import com.fx.global.domain.readingroom.SeatAlert.SeatAlertStatus
import com.fx.global.exception.FcmTokenException
import com.fx.global.exception.ReadingRoomException
import com.fx.global.exception.errorcode.FcmTokenErrorCode
import com.fx.global.exception.errorcode.ReadingRoomErrorCode
import com.mongodb.DuplicateKeyException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ReadingRoomCommandService(
    private val fcmTokenPersistencePort: FcmTokenPersistencePort,
    private val readingRoomRemotePort: ReadingRoomRemotePort,
    private val seatAlertPersistencePort: SeatAlertPersistencePort
) : ReadingRoomCommandUseCase {

    private val log = LoggerFactory.getLogger(ReadingRoomCommandService::class.java)

    override suspend fun createSeatAlert(seatAlertCommand: CreateSeatAlertCommand): SeatAlert {
        // 1. FCM 토큰 유효성 검사
        val fcmToken = fcmTokenPersistencePort.findByFcmToken(seatAlertCommand.fcmToken)
            ?: throw FcmTokenException(FcmTokenErrorCode.TOKEN_NOT_FOUND)

        // 2. 열람실 좌석 정보 조회
        val seat = readingRoomRemotePort.getReadingRoomSeats(
            seatAlertCommand.readingRoom.roomId,
            readingRoomRemotePort.getCsrfToken()
        ).find { it.seatId == seatAlertCommand.seatNumber }
            ?: throw ReadingRoomException(ReadingRoomErrorCode.SEAT_NOT_FOUND)

        // 3. 좌석이 비어있는 경우 알림 등록하지 않음
        if (seat.isAvailable) {
            throw ReadingRoomException(ReadingRoomErrorCode.SEAT_ALREADY_AVAILABLE)
        }

        val activeSeatAlerts = seatAlertPersistencePort.findByFcmTokenAndStatus(
            fcmToken.fcmToken, SeatAlertStatus.ACTIVE
        )

        // 4. 최대 5개까지 알림 등록 가능
        if (activeSeatAlerts.size >= 5) {
            throw ReadingRoomException(ReadingRoomErrorCode.MAX_SEAT_ALERT_LIMIT_EXCEEDED)
        }

        // 5. 이미 해당 좌석에 대한 활성화된 알림이 존재하는지 확인
        if (activeSeatAlerts.any {
                it.readingRoom == seatAlertCommand.readingRoom && it.seatNumber == seatAlertCommand.seatNumber
            }) {
            throw ReadingRoomException(ReadingRoomErrorCode.SEAT_ALERT_ALREADY_EXISTS)
        }

        // 6. 좌석 알림 등록
        return try {
            seatAlertPersistencePort.save(
                SeatAlert.createSeatAlert(
                    fcmToken.fcmToken,
                    seatAlertCommand.readingRoom,
                    seatAlertCommand.seatNumber
                )
            )
        } catch (ex: DuplicateKeyException) {
            log.warn("중복 알림 등록 시도: $seatAlertCommand", ex)
            throw ReadingRoomException(ReadingRoomErrorCode.SEAT_ALERT_ALREADY_EXISTS)
        }
    }

}