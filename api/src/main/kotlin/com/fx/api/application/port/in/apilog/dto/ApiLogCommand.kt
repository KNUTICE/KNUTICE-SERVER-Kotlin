package com.fx.api.application.port.`in`.apilog.dto

import java.time.LocalDateTime

/**
 * 요청 한 건의 기록. 요청 스레드에서 만들므로 변환하지 않은 값을 그대로 담는다.
 *
 * @property topicName 공지 목록 v1 의 `topic` 파라미터 (토픽 이름)
 * @property topicId 공지 목록 v2 의 `topicId` 파라미터 (토픽 코드)
 * @property fcmToken `fcmToken` 헤더. 저장할 때 토큰 ID 로 바꾸고 원문은 저장하지 않는다
 */
data class ApiLogCommand(
    val requestedAt: LocalDateTime,
    val method: String,
    val urlPattern: String,
    val statusCode: Int,
    val durationMs: Int,
    val topicName: String? = null,
    val topicId: String? = null,
    val fcmToken: String? = null,
)
