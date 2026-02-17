package com.fx.readingroom.adapter.out.web.dto

data class ReadingRoomStatusRemoteResponse(
    val result: StatusData
)

data class StatusData(
    val CODE: String,
    val items: List<StatusItem> = emptyList()
)

data class StatusItem(
    val room_no: Int,
    val name: String,
    val total_count: Int,
    val usage_count: Int,
    val remain_count: Int,
    val rows: Int,
    val cols: Int
)