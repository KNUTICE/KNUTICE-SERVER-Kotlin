package com.fx.crawler.legacy.readingroom

import com.fx.common.annotation.PersistenceAdapter
import com.fx.crawler.legacy.readingroom.SeatAlertPersistencePort
import com.fx.crawler.legacy.readingroom.SeatAlert
import com.fx.crawler.legacy.readingroom.SeatAlert.SeatAlertStatus

@PersistenceAdapter
class SeatAlertPersistenceAdapter(
    private val seatAlertMongoRepository: SeatAlertMongoRepository
): SeatAlertPersistencePort {

    override fun save(seatAlert: SeatAlert): SeatAlert =
        seatAlertMongoRepository.save(SeatAlertDocument.from(seatAlert)).toDomain()

    override fun findByFcmTokenAndStatus(fcmToken: String, seatAlertStatus: SeatAlertStatus): List<SeatAlert> =
        seatAlertMongoRepository.findByFcmTokenAndStatusOrderByCreatedAtDesc(fcmToken, seatAlertStatus)
            .map { it.toDomain() }

    override fun deleteByFcmTokenAndAlertId(fcmToken: String, alertId: String): Boolean {
        return seatAlertMongoRepository.deleteByFcmTokenAndId(fcmToken, alertId) > 0
    }

    override fun delete(alertId: String) {
        seatAlertMongoRepository.deleteById(alertId)
    }

    override fun findByAlertStatus(seatAlertStatus: SeatAlertStatus): List<SeatAlert> =
        seatAlertMongoRepository.findByStatus(seatAlertStatus).map { it.toDomain() }

}