package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.SeatAlertMongoRepository
import com.fx.api.application.port.out.readingroom.SeatAlertPersistencePort
import com.fx.global.adapter.out.persistence.document.SeatAlertDocument
import com.fx.global.annotation.PersistenceAdapter
import com.fx.global.domain.readingroom.SeatAlert
import com.fx.global.domain.readingroom.SeatAlert.SeatAlertStatus

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

}