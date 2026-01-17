package com.fx.api.domain

import java.time.LocalDateTime

data class ReadingRoom(

    val roomId: Int, // 열람실 번호
    val seatId: Int, // 좌석 번호
    val xPosition: Int, // X 좌표
    val yPosition: Int, // Y 좌표

    val isAvailable: Boolean, // 사용 가능 여부
    val userMaskedName: String? = null, // 사용자 마스킹 이름
    val returnAt: LocalDateTime // 반납 시간

)
