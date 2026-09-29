package com.fx.crawler.legacy.readingroom

import com.fx.crawler.legacy.readingroom.SeatAlert
import com.fx.crawler.legacy.readingroom.SeatAlert.SeatAlertStatus

interface SeatAlertPersistencePort {

    fun save(seatAlert: SeatAlert): SeatAlert

    fun findByFcmTokenAndStatus(fcmToken: String, seatAlertStatus: SeatAlertStatus): List<SeatAlert>

    fun deleteByFcmTokenAndAlertId(fcmToken: String, alertId: String): Boolean

    fun delete(alertId: String)

    fun findByAlertStatus(seatAlertStatus: SeatAlertStatus): List<SeatAlert>

}