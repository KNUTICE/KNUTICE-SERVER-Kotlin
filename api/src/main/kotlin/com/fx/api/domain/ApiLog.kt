package com.fx.api.domain

import com.fx.common.domain.catalog.TopicView
import com.fx.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 * 앱 Open API 요청 한 건. 일일 통계의 원본이며 쓰기만 한다.
 *
 * - 통계 날짜는 [requestedAt](KST)으로 정한다. 묶어서 저장하므로 `created_at` 은 요청보다 늦다.
 * - 경로 변수 · 쿼리 문자열은 남기지 않고 [urlPattern] 만 남긴다. 공지 목록 조회의 토픽만 따로 남긴다.
 */
@Entity
@Table(
    name = "api_log",
    indexes = [
        // 날짜별 집계 · 보관 기간 삭제 : `WHERE requested_at >= ? AND requested_at < ?`
        Index(name = "idx_api_log_requested_at", columnList = "requested_at"),
    ],
)
class ApiLog(
    requestedAt: LocalDateTime,
    method: String,
    urlPattern: String,
    statusCode: Int,
    durationMs: Int,
    topic: TopicView?,
    fcmTokenId: Long?,
) : BaseEntity() {

    @Column(name = "requested_at", nullable = false, updatable = false, comment = "요청 시각 (KST)")
    val requestedAt: LocalDateTime = requestedAt

    @Column(name = "method", nullable = false, updatable = false, length = 10, comment = "HTTP 메서드")
    val method: String = method

    @Column(name = "url_pattern", nullable = false, updatable = false, length = 200, comment = "요청이 매핑된 URL 패턴")
    val urlPattern: String = urlPattern

    @Column(name = "status_code", nullable = false, updatable = false, comment = "응답 상태 코드")
    val statusCode: Int = statusCode

    @Column(name = "duration_ms", nullable = false, updatable = false, comment = "처리 시간 (밀리초)")
    val durationMs: Int = durationMs

    @Column(name = "topic_code", updatable = false, comment = "공지 목록 조회의 토픽 코드 (v2 topicId)")
    val topicCode: Int? = topic?.code

    @Column(name = "topic_name", updatable = false, length = 100, comment = "공지 목록 조회의 토픽 이름 (v1 topic)")
    val topicName: String? = topic?.name

    @Column(name = "fcm_token_id", updatable = false, comment = "요청한 기기의 FCM 토큰 ID")
    val fcmTokenId: Long? = fcmTokenId

}
