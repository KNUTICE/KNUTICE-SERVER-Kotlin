package com.fx.api.application.port.`in`

import com.fx.api.domain.ReadingRoom

/**
 * 열람실 관련 조회를 위한 Input Port 입니다.
 *
 * @author 이동섭
 * @since 2026-01-17
 */
interface ReadingRoomQueryUseCase {

    suspend fun getReadingRooms(fcmToken: String, roomId: Int): List<ReadingRoom>

}