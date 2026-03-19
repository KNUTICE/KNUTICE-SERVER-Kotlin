package com.fx.readingroom.adapter.out.persistence

import com.fx.global.annotation.PersistenceAdapter
import com.fx.readingroom.adapter.out.persistence.document.SeatAlertDocument
import com.fx.readingroom.adapter.out.persistence.repository.SeatAlertMongoRepository
import com.fx.readingroom.application.port.out.SeatAlertPersistencePort
import com.fx.readingroom.domain.SeatAlert
import com.fx.readingroom.domain.SeatAlert.SeatAlertStatus
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

@PersistenceAdapter
class SeatAlertPersistenceAdapter(
    private val seatAlertMongoRepository: SeatAlertMongoRepository
): SeatAlertPersistencePort {

    override suspend fun save(seatAlert: SeatAlert): SeatAlert =
        seatAlertMongoRepository.save(SeatAlertDocument.from(seatAlert)).toDomain()

    override suspend fun findByFcmTokenAndStatus(fcmToken: String, seatAlertStatus: SeatAlertStatus): List<SeatAlert> =
        seatAlertMongoRepository.findByFcmTokenAndStatusOrderByCreatedAtDesc(fcmToken, seatAlertStatus)
            .map { it.toDomain() }
            .toList()

    override suspend fun deleteByFcmTokenAndAlertId(fcmToken: String, alertId: String): Boolean {
        return seatAlertMongoRepository.deleteByFcmTokenAndId(fcmToken, alertId) > 0
    }

    override suspend fun delete(alertId: String) {
        seatAlertMongoRepository.deleteById(alertId)
    }

    override suspend fun findByAlertStatus(seatAlertStatus: SeatAlertStatus): List<SeatAlert> =
        seatAlertMongoRepository.findByStatus(seatAlertStatus)
            .map { it.toDomain() }
            .toList()

}