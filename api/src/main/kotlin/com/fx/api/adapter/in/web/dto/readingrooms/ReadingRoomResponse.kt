package com.fx.api.adapter.`in`.web.dto.readingrooms

import com.fx.api.domain.ReadingRoom
import java.time.LocalDateTime

/**
 * 열람실 응답 DTO
 *
 * @author 이동섭
 * @since 2026-01-17
 */
data class ReadingRoomResponse(

    val roomId: Int, // 열람실 번호
    val seatId: Int, // 좌석 번호
    val xPosition: Int, // X 좌표
    val yPosition: Int, // Y 좌표

    val isAvailable: Boolean, // 사용 가능 여부
    val userMaskedName: String? = null, // 사용자 마스킹 이름
    val returnAt: LocalDateTime // 반납 시간

) {
    companion object {
        fun from(readingRooms: List<ReadingRoom>): List<ReadingRoomResponse> {
            TODO("Not yet implemented")
        }
    }
}
