package com.fx.migration.target

import com.fx.common.domain.DeviceType
import com.fx.common.domain.notice.SummaryStatus
import java.time.LocalDate
import java.time.LocalDateTime

/** 토픽 이름 → 코드 매핑에 쓰는 `topic` 행. */
data class TopicRef(val code: Int, val name: String)

/** `notice` 행과 딸린 `notice_content` 행. 이관한 공지는 모두 발송 완료 상태다. */
data class NoticeRow(
    val id: Long,
    val nttId: Long,
    val topic: TopicRef,
    val title: String,
    val department: String,
    val contentUrl: String,
    val contentImageUrl: String?,
    val registrationDate: LocalDate,
    val isAttachment: Boolean,
    val notifiedAt: LocalDateTime,
    val summaryStatus: SummaryStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val contentId: Long,
    val content: String?,
    val contentSummary: String?,
)

data class FcmTokenRow(
    val id: Long,
    val token: String,
    val deviceType: DeviceType,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val subscriptions: List<SubscriptionRow>,
)

data class SubscriptionRow(val id: Long, val topic: TopicRef)

data class UserRow(
    val id: Long,
    val email: String,
    val password: String,
    val nickname: String,
    val role: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

data class TipRow(
    val id: Long,
    val title: String,
    val url: String,
    val deviceType: DeviceType,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

data class ImageRow(
    val id: Long,
    val imageUrl: String,
    val originalName: String,
    val serverName: String,
    val extension: String,
    val imageType: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

/** 레거시 문의는 토큰 문자열을 가지고 있다. `fcm_token_id` 는 쓸 때 토큰으로 찾는다. */
data class ReportRow(
    val id: Long,
    val token: String,
    val content: String,
    val deviceName: String,
    val version: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
