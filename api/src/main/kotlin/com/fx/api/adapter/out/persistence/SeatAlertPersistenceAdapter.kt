package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.SeatAlertMongoRepository
import com.fx.api.application.port.out.readingroom.SeatAlertPersistencePort
import com.fx.global.adapter.out.persistence.document.SeatAlertDocument
import com.fx.global.annotation.PersistenceAdapter
import com.fx.global.domain.readingroom.SeatAlert

@PersistenceAdapter
class SeatAlertPersistenceAdapter(
    private val seatAlertMongoRepository: SeatAlertMongoRepository
): SeatAlertPersistencePort {

    override fun save(seatAlert: SeatAlert) {
        seatAlertMongoRepository.save(SeatAlertDocument.from(seatAlert))
    }

}