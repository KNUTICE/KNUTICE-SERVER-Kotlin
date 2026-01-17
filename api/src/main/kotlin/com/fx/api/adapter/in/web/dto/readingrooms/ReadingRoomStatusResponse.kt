package com.fx.api.adapter.`in`.web.dto.readingrooms

import com.fx.api.domain.ReadingRoomStatus

/**
 * 열람실 정보 응답 DTO
 *
 * @author 이동섭
 * @since 2026-01-17
 */
data class ReadingRoomStatusResponse(

    val roomId: Int, // 열람실 번호
    val roomName: String, // 열람실 이름
    val totalSeat: Int, // 총 좌석 수
    val availableSeat: Int, // 잔여 좌석 수
    val occupiedSeat: Int, // 사용 중 좌석 수

    // 행/열 정보
    val rowCount: Int,
    val columnCount: Int

) {
    companion object {

        fun from(readingRoomStatus: ReadingRoomStatus): ReadingRoomStatusResponse =
            ReadingRoomStatusResponse(
                roomId = readingRoomStatus.roomId,
                roomName = readingRoomStatus.roomName,
                totalSeat = readingRoomStatus.totalSeat,
                availableSeat = readingRoomStatus.availableSeat,
                occupiedSeat = readingRoomStatus.occupiedSeat,
                rowCount = readingRoomStatus.rowCount,
                columnCount = readingRoomStatus.columnCount
            )

        fun from(readingRoomStatus: List<ReadingRoomStatus>): List<ReadingRoomStatusResponse> =
            readingRoomStatus.map { this.from(it) }

    }
}
