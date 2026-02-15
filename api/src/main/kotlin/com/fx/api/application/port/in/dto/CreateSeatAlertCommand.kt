package com.fx.api.application.port.`in`.dto

import com.fx.global.domain.readingroom.SeatAlert.ReadingRoom

data class CreateSeatAlertCommand(

    val fcmToken: String, // FCM 토큰
    val readingRoom: ReadingRoom, // 열람실 이름
    val seatNumber: Int, // 좌석 번호

)