package com.fx.readingroom.adapter.out.web.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ReadingRoomStatusRemoteResponse(
    @param:JsonProperty("result") val result: StatusData,
)

data class StatusData(
    @param:JsonProperty("CODE") val code: String,
    @param:JsonProperty("items") val items: List<StatusItem> = emptyList(),
)

data class StatusItem(
    @param:JsonProperty("room_no") val roomNo: Int,
    @param:JsonProperty("name") val name: String,
    @param:JsonProperty("total_count") val totalCount: Int,
    @param:JsonProperty("usage_count") val usageCount: Int,
    @param:JsonProperty("remain_count") val remainCount: Int,
    @param:JsonProperty("rows") val rows: Int,
    @param:JsonProperty("cols") val cols: Int,
)
