package com.fx.api.application.port.`in`.readingroom

import com.fx.api.application.port.`in`.dto.CreateSeatAlertCommand

interface ReadingRoomCommandUseCase {

    suspend fun createSeatAlert(seatAlertCommand: CreateSeatAlertCommand): String

}