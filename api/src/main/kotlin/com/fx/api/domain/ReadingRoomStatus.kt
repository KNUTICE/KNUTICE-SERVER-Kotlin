package com.fx.api.domain

import com.fx.global.domain.readingroom.ReadingRoom

data class ReadingRoomStatus(

    val roomId: ReadingRoom, // 열람실 번호
    val roomName: String, // 열람실 이름
    val totalSeat: Int, // 총 좌석 수
    val availableSeat: Int, // 잔여 좌석 수
    val occupiedSeat: Int, // 사용 중 좌석 수

    // 행/열 정보
    val rowCount: Int,
    val columnCount: Int

)
