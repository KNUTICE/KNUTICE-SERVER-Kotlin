package com.fx.api.application.port.`in`.readingroom

import com.fx.api.application.port.`in`.dto.CreateSeatAlertCommand
import com.fx.readingroom.domain.SeatAlert

interface ReadingRoomCommandUseCase {

    suspend fun createSeatAlert(seatAlertCommand: CreateSeatAlertCommand): SeatAlert

    suspend fun deleteSeatAlert(fcmToken: String, alertId: String): Boolean

}