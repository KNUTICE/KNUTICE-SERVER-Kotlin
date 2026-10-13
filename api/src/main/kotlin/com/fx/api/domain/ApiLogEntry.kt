package com.fx.api.domain

import com.fx.common.domain.DeviceType
import java.time.LocalDateTime

/** 원본 API 로그 한 건 (`api_log`) */
data class ApiLogEntry(
    val id: Long,
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
)

/** 응답 상태 코드 묶음 */
enum class ApiLogStatusGroup(
    val statusCodes: IntRange,
) {

    /** 4xx */
    CLIENT_ERROR(400..499),

    /** 5xx */
    SERVER_ERROR(500..599),

}
