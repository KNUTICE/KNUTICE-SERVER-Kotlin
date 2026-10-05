package com.fx.readingroom.adapter.out.web.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ReadingRoomSeatRemoteResponse(
    @param:JsonProperty("result") val result: SeatData,
)

data class SeatData(
    @param:JsonProperty("CODE") val code: String,
    @param:JsonProperty("items") val items: List<SeatItem> = emptyList(),
)

data class SeatItem(
    @param:JsonProperty("room_no") val roomNo: Int,
    @param:JsonProperty("number") val number: Int,
    @param:JsonProperty("x_pos") val xPos: Int,
    @param:JsonProperty("y_pos") val yPos: Int,
    @param:JsonProperty("use_type") val useType: Int,
    @param:JsonProperty("seat_return") val seatReturn: Long, // epoch milliseconds
    @param:JsonProperty("user_name") val userName: String?, // use_type = 1 인 경우에만 존재함
)
