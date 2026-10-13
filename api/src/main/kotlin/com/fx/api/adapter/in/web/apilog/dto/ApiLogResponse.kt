package com.fx.api.adapter.`in`.web.apilog.dto

import com.fx.api.domain.ApiLogEntry
import com.fx.common.domain.DeviceType
import java.time.LocalDateTime

data class ApiLogResponse(
    /** TSID 문자열 */
    val id: String,
    val requestedAt: LocalDateTime,
    val method: String,
    val urlPattern: String,
    val statusCode: Int,
    val durationMs: Int,
    /** 공지 목록 조회의 토픽. 그 밖의 요청은 null */
    val topicCode: Int?,
    val topicName: String?,
    /** 요청한 기기의 종류. 토큰 헤더가 없거나 등록되지 않은 토큰이면 null */
    val deviceType: DeviceType?,
) {

    companion object {

        fun from(entry: ApiLogEntry): ApiLogResponse =
            ApiLogResponse(
                id = entry.id.toString(),
                requestedAt = entry.requestedAt,
                method = entry.method,
                urlPattern = entry.urlPattern,
                statusCode = entry.statusCode,
                durationMs = entry.durationMs,
                topicCode = entry.topicCode,
                topicName = entry.topicName,
                deviceType = entry.deviceType,
            )

    }

}
