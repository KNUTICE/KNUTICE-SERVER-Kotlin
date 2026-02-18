package com.fx.crawler.appllication.service

import com.fx.crawler.appllication.port.`in`.NotificationUseCase
import com.fx.crawler.appllication.port.`in`.readingroom.SeatAlertProcessUseCase
import com.fx.readingroom.application.port.out.ReadingRoomRemotePort
import com.fx.readingroom.application.port.out.SeatAlertPersistencePort
import com.fx.readingroom.domain.ReadingRoom
import com.fx.readingroom.domain.SeatAlert
import com.fx.readingroom.domain.SeatAlert.SeatAlertStatus
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SeatAlertProcessService(
    private val seatAlertPersistencePort: SeatAlertPersistencePort,
    private val readingRoomRemotePort: ReadingRoomRemotePort,
    private val notificationUseCase: NotificationUseCase
) : SeatAlertProcessUseCase {

    private val log = LoggerFactory.getLogger(SeatAlertProcessService::class.java)

    override suspend fun checkAndNotifyAll() = coroutineScope {
        // 1. ACTIVE 알림 조회
        val alerts = seatAlertPersistencePort.findByAlertStatus(SeatAlertStatus.ACTIVE)
        if (alerts.isEmpty()) return@coroutineScope

        val csrfToken = readingRoomRemotePort.getCsrfToken()

        // 2. 열람실별 그룹화
        val alertsByRoom = alerts.groupBy { it.readingRoom }

        // 3. 열람실별 체크 및 전송
        alertsByRoom.map { (room, roomAlerts) ->
            async {
                try {
                    val currentSeats = readingRoomRemotePort.getReadingRoomSeats(room, csrfToken)
                    val availableSeats = currentSeats.filter { it.isAvailable }.map { it.seatNumber }.toSet()

                    roomAlerts.forEach { alert ->
                        if (alert.seatNumber in availableSeats) {
                            triggerAlert(alert)
                        }
                    }
                } catch (e: Exception) {
                    log.error("열람실 빈자리 알림 중 오류 발생 : ${e.message}")
                }
            }
        }.awaitAll()
    }

    private suspend fun triggerAlert(alert: SeatAlert) {
        notificationUseCase.sendSeatAlert(alert)
        seatAlertPersistencePort.delete(alert.id!!)
    }

}