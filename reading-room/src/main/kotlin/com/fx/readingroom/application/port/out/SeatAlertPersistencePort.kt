package com.fx.readingroom.application.port.out

import com.fx.readingroom.domain.SeatAlert
import com.fx.readingroom.domain.SeatAlert.SeatAlertStatus

interface SeatAlertPersistencePort {

    suspend fun save(seatAlert: SeatAlert): SeatAlert

    suspend fun findByFcmTokenAndStatus(fcmToken: String, seatAlertStatus: SeatAlertStatus): List<SeatAlert>

    suspend fun deleteByFcmTokenAndAlertId(fcmToken: String, alertId: String): Boolean

    suspend fun delete(alertId: String)

    suspend fun findByAlertStatus(seatAlertStatus: SeatAlertStatus): List<SeatAlert>

}