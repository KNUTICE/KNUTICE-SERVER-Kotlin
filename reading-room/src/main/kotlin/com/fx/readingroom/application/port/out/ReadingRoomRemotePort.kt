package com.fx.readingroom.application.port.out

import com.fx.readingroom.domain.ReadingRoom
import com.fx.readingroom.domain.ReadingRoomSeat
import com.fx.readingroom.domain.ReadingRoomStatus

interface ReadingRoomRemotePort {

    /** 열람실 사이트에 접속해 세션을 맺고 좌석 조회에 쓸 CSRF 토큰을 받는다. */
    fun getCsrfToken(): String

    fun getReadingRoomStatus(): List<ReadingRoomStatus>

    fun getReadingRoomSeats(readingRoom: ReadingRoom, csrfToken: String): List<ReadingRoomSeat>

}
