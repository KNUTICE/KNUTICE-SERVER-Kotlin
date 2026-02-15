package com.fx.api.application.port.`in`.readingroom

import com.fx.api.application.port.`in`.dto.CreateSeatAlertCommand
import com.fx.global.domain.readingroom.SeatAlert

interface ReadingRoomCommandUseCase {

    suspend fun createSeatAlert(seatAlertCommand: CreateSeatAlertCommand): SeatAlert

}