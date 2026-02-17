package com.fx.readingroom.application.port.out

import com.fx.readingroom.domain.SeatAlert
import com.fx.readingroom.domain.SeatAlert.SeatAlertStatus

interface SeatAlertPersistencePort {

    fun save(seatAlert: SeatAlert): SeatAlert

    fun findByFcmTokenAndStatus(fcmToken: String, seatAlertStatus: SeatAlertStatus): List<SeatAlert>

    fun deleteByFcmTokenAndAlertId(fcmToken: String, alertId: String): Boolean

}