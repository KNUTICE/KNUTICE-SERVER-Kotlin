package com.fx.api.application.port.`in`.readingroom

import com.fx.api.application.port.`in`.readingroom.dto.CreateSeatAlertCommand
import com.fx.readingroom.domain.SeatAlert

interface ReadingRoomCommandUseCase {

    fun createSeatAlert(seatAlertCommand: CreateSeatAlertCommand): SeatAlert

    fun deleteSeatAlert(fcmToken: String, alertId: String): Boolean

}
