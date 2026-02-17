package com.fx.api.application.port.out.readingroom

import com.fx.global.domain.readingroom.SeatAlert
import com.fx.global.domain.readingroom.SeatAlert.SeatAlertStatus

interface SeatAlertPersistencePort {

    fun save(seatAlert: SeatAlert): SeatAlert

    fun findByFcmTokenAndStatus(fcmToken: String, seatAlertStatus: SeatAlertStatus): List<SeatAlert>

    fun deleteByFcmTokenAndAlertId(fcmToken: String, alertId: String): Boolean

}