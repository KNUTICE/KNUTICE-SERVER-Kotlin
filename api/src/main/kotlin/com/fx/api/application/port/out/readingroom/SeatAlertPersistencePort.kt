package com.fx.api.application.port.out.readingroom

import com.fx.global.domain.readingroom.SeatAlert

interface SeatAlertPersistencePort {

    fun save(seatAlert: SeatAlert)

}