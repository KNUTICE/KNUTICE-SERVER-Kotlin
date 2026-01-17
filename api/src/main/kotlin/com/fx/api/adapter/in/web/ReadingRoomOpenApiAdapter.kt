package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.dto.readingrooms.ReadingRoomSeatResponse
import com.fx.api.adapter.`in`.web.dto.readingrooms.ReadingRoomStatusResponse
import com.fx.api.adapter.`in`.web.swagger.ReadingRoomOpenApiSwagger
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
) : ReadingRoomOpenApiSwagger {

    @GetMapping("/status")
    override suspend fun getReadingRoomStatus(
        @RequestHeader fcmToken: String
    ): ResponseEntity<Api<List<ReadingRoomStatusResponse>>> =
        Api.OK(ReadingRoomStatusResponse.from(
            readingRoomQueryUseCase.getReadingRoomStatus(fcmToken)
        ))

    @GetMapping("/{roomId}")
    override suspend fun getReadingRoomSeats(
        @RequestHeader fcmToken: String,
        @PathVariable roomId: Int
    ): ResponseEntity<Api<List<ReadingRoomSeatResponse>>> =
        Api.OK(ReadingRoomSeatResponse.from(
            readingRoomQueryUseCase.getReadingRoomSeats(fcmToken, roomId)
        ))

}