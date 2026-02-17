package com.fx.readingroom.adapter.out.web.dto

data class ReadingRoomSeatRemoteResponse(
    val result: SeatData
)

data class SeatData(
    val CODE: String,
    val items: List<SeatItem> = emptyList()
)

data class SeatItem(
    val room_no: Int,
    val number: Int,
    val x_pos: Int,
    val y_pos: Int,
    val use_type: Int,
    val seat_return: Long, // epoch milliseconds
    val user_name: String? // use_type = 1 인 경우에만 존재함
)