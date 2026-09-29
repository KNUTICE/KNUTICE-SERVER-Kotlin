package com.fx.crawler.application.service

import com.fx.common.application.port.`in`.CatalogQueryUseCase
import com.fx.common.concurrent.BoundedParallelExecutor
import com.fx.crawler.application.port.`in`.PushSendUseCase
import com.fx.crawler.application.port.`in`.SeatAlertCheckUseCase
import com.fx.crawler.config.CrawlerProperties
import com.fx.crawler.domain.push.PushTarget
import com.fx.crawler.domain.push.SeatAlertPushComposer
import com.fx.readingroom.application.port.out.ReadingRoomRemotePort
import com.fx.readingroom.application.port.out.SeatAlertPersistencePort
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDateTime

/**
 * 열람실 빈자리 확인. 알림이 걸린 열람실만 조회하고, 빈 좌석의 알림은 보낸 뒤 바로 지운다.
 * 열람실 사이트를 기다리는 동안 DB 커넥션을 잡지 않도록 트랜잭션을 걸지 않는다.
 */
@Service
class SeatAlertCheckService(
    private val seatAlertPersistencePort: SeatAlertPersistencePort,
    private val readingRoomRemotePort: ReadingRoomRemotePort,
    private val pushSendUseCase: PushSendUseCase,
    private val catalogQueryUseCase: CatalogQueryUseCase,
    @param:Qualifier("readingRoomSiteExecutor") private val readingRoomSiteExecutor: BoundedParallelExecutor,
    private val properties: CrawlerProperties,
    private val clock: Clock,
) : SeatAlertCheckUseCase {

    private val log = LoggerFactory.getLogger(SeatAlertCheckService::class.java)

    override fun deleteExpired(): Int =
        seatAlertPersistencePort.deleteExpired(LocalDateTime.now(clock))

    override fun checkAndNotify(): Int {
        val alertsByRoom = seatAlertPersistencePort.findActiveTargets(LocalDateTime.now(clock))
            .groupBy { it.readingRoom }
        if (alertsByRoom.isEmpty()) {
            return 0
        }

        val csrfToken = readingRoomRemotePort.getCsrfToken()
        val rooms = alertsByRoom.keys.toList()
        val seatResults = readingRoomSiteExecutor.invokeAll(
            rooms.map { room -> { readingRoomRemotePort.getReadingRoomSeats(room, csrfToken) } },
            properties.seatAlert.timeout,
        )
        val templates = catalogQueryUseCase.getNotificationTemplateCatalog()

        var notified = 0
        rooms.zip(seatResults).forEach { (room, result) ->
            val seats = result.getOrElse {
                log.warn("열람실 좌석 조회 실패 - room: {}, {}", room, it.message)
                return@forEach
            }
            val availableSeats = seats.filter { it.isAvailable }.map { it.seatNumber }.toSet()

            alertsByRoom.getValue(room)
                .filter { it.seatNumber in availableSeats }
                .forEach { alert ->
                    pushSendUseCase.send(
                        listOf(PushTarget(alert.fcmTokenId, alert.token, alert.language)),
                        SeatAlertPushComposer.compose(alert.readingRoom, alert.seatNumber, templates),
                    )
                    seatAlertPersistencePort.deleteById(alert.seatAlertId)
                    notified++
                }
        }
        return notified
    }

}
