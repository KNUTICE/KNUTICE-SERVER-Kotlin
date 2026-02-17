package com.fx.readingroom.domain

import java.time.LocalDateTime

data class ReadingRoomSeat(

    val roomId: ReadingRoom, // 열람실 번호
    val seatNumber: Int, // 좌석 번호

    val row: Int,    // 세로 위치
    val column: Int, // 가로 위치

    val isAvailable: Boolean, // 사용 가능 여부
    val userMaskedName: String? = null, // 사용자 마스킹 이름
    val returnAt: LocalDateTime // 반납 시간

)
