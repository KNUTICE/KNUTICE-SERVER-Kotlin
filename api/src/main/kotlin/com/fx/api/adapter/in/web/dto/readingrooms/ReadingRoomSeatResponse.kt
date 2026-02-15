package com.fx.api.adapter.`in`.web.dto.readingrooms

import com.fx.api.domain.ReadingRoomSeat
import com.fx.global.domain.readingroom.ReadingRoom
import java.time.LocalDateTime

/**
 * 열람실 좌석 응답 DTO
 *
 * @author 이동섭
 * @since 2026-01-17
 */
data class ReadingRoomSeatResponse(

    val roomId: ReadingRoom, // 열람실 번호
    val seatNumber: Int, // 좌석 번호

    val row: Int,    // 세로 위치
    val column: Int, // 가로 위치

    val isAvailable: Boolean, // 사용 가능 여부
//    val userMaskedName: String? = null, // 사용자 마스킹 이름? // dslee (2026-01-31) : 이름 응답 안함
    val returnAt: LocalDateTime // 반납 시간

) {
    companion object {

        fun from(readingRoomSeat: ReadingRoomSeat): ReadingRoomSeatResponse =
            ReadingRoomSeatResponse(
                roomId = readingRoomSeat.roomId,
                seatNumber = readingRoomSeat.seatNumber,
                row = readingRoomSeat.row,
                column = readingRoomSeat.column,
                isAvailable = readingRoomSeat.isAvailable,
//                userMaskedName = readingRoomSeat.userMaskedName,
                returnAt = readingRoomSeat.returnAt
            )

        fun from(readingRoomSeats: List<ReadingRoomSeat>): List<ReadingRoomSeatResponse> =
            readingRoomSeats.map { this.from(it) }

    }
}
