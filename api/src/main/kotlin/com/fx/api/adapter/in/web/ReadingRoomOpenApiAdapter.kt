package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.dto.readingrooms.ReadingRoomResponse
import com.fx.api.application.port.`in`.ReadingRoomQueryUseCase
import com.fx.global.annotation.hexagonal.WebInputAdapter
import io.github.seob7.Api
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping

@WebInputAdapter
@RequestMapping("/open-api/v1/reading-rooms")
class ReadingRoomOpenApiAdapter(
    private val readingRoomQueryUseCase: ReadingRoomQueryUseCase
) {

    @GetMapping("/{roomId}")
    suspend fun getReadingRooms(
        @RequestHeader fcmToken: String,
        @PathVariable roomId: Int
    ): ResponseEntity<Api<List<ReadingRoomResponse>>> =
        Api.OK(ReadingRoomResponse.from(
            readingRoomQueryUseCase.getReadingRooms(fcmToken, roomId)
        ))

}