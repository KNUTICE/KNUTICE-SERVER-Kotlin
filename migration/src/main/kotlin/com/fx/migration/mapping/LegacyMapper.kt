package com.fx.migration.mapping

import com.fx.common.domain.DeviceType
import com.fx.common.domain.fcmtoken.FCM_TOKEN_MAX_LENGTH
import com.fx.common.domain.notice.NOTICE_DEPARTMENT_MAX_LENGTH
import com.fx.common.domain.notice.NOTICE_TITLE_MAX_LENGTH
import com.fx.common.domain.notice.NOTICE_URL_MAX_LENGTH
import com.fx.common.domain.notice.SummaryStatus
import com.fx.migration.legacy.LegacyDocument
import com.fx.migration.target.FcmTokenRow
import com.fx.migration.target.ImageRow
import com.fx.migration.target.NoticeRow
import com.fx.migration.target.ReportRow
import com.fx.migration.target.SubscriptionRow
import com.fx.migration.target.TipRow
import com.fx.migration.target.TopicRef
import com.fx.migration.target.UserRow
import java.time.LocalDateTime

/** 도큐먼트 하나의 변환 결과. */
sealed interface Mapped<out T> {

    /** @property notes 값을 고쳐서 옮긴 내역 (길이 초과로 자름 등). 건수만 세므로 값은 넣지 않는다 */
    data class Row<out T>(val row: T, val notes: List<String> = emptyList()) : Mapped<T>

    data class Skipped(val reason: String) : Mapped<Nothing>

}

/**
 * 레거시 도큐먼트를 새 스키마의 행으로 바꾼다.
 *
 * - 레거시 생성 · 수정 시각을 그대로 옮긴다. 없으면 이관 시각([now])을 쓴다.
 * - 새 스키마의 컬럼 길이를 넘는 값은 자르고, 잘라서는 안 되는 값(URL · 파일명 · 식별자)이 넘치면 건너뛴다.
 * - 토픽은 레거시 enum 이름(= `topic.name`)으로 [topics] 에서 코드를 찾는다.
 */
class LegacyMapper(
    private val topics: Map<String, TopicRef>,
    private val now: LocalDateTime,
    private val newId: () -> Long,
) {

    /** 기존 공지는 전부 발송 완료로, 요약은 있으면 완료 · 없으면 건너뜀으로 옮긴다 (과거 공지의 재발송 · 재요약 방지). */
    fun notice(document: LegacyDocument): Mapped<NoticeRow> {
        val nttId = (document.id as? Number)?.toLong() ?: return Mapped.Skipped("게시글 번호 없음")
        val topicName = document.string("topic") ?: return Mapped.Skipped("토픽 없음")
        val topic = topics[topicName] ?: return Mapped.Skipped("모르는 토픽: $topicName")
        val contentUrl = document.string("contentUrl")?.takeIf { it.isNotBlank() } ?: return Mapped.Skipped("원문 URL 없음")
        if (contentUrl.length > NOTICE_URL_MAX_LENGTH) {
            return Mapped.Skipped("원문 URL 길이 초과")
        }

        val notes = mutableListOf<String>()
        val (createdAt, updatedAt) = timestamps(document, notes)
        val contentSummary = document.string("contentSummary")?.takeIf { it.isNotBlank() }
        val contentImageUrl = document.string("contentImageUrl")?.takeIf { it.isNotBlank() }
            ?.let { url -> url.takeIf { it.length <= NOTICE_URL_MAX_LENGTH } ?: null.also { notes += "이미지 URL 길이 초과로 뺌" } }

        return Mapped.Row(
            NoticeRow(
                id = newId(),
                nttId = nttId,
                topic = topic,
                title = limit(document.string("title").orEmpty(), NOTICE_TITLE_MAX_LENGTH, "제목", notes),
                department = limit(document.string("department").orEmpty(), NOTICE_DEPARTMENT_MAX_LENGTH, "부서", notes),
                contentUrl = contentUrl,
                contentImageUrl = contentImageUrl,
                registrationDate = document.date("registrationDate")
                    ?: createdAt.toLocalDate().also { notes += "게시일 없음 → 생성일" },
                isAttachment = document.boolean("isAttachment", "attachment") ?: false,
                notifiedAt = createdAt,
                summaryStatus = if (contentSummary != null) SummaryStatus.COMPLETED else SummaryStatus.SKIPPED,
                createdAt = createdAt,
                updatedAt = updatedAt,
                contentId = newId(),
                content = document.string("content")?.takeIf { it.isNotBlank() },
                contentSummary = contentSummary,
            ),
            notes,
        )
    }

    /** 구독은 세 enum 집합을 합쳐 토픽 코드 · 이름으로 옮긴다. 알림 언어는 모두 기본값(ko)이다. */
    fun fcmToken(document: LegacyDocument): Mapped<FcmTokenRow> {
        val token = document.id?.toString()?.takeIf { it.isNotBlank() } ?: return Mapped.Skipped("토큰 없음")
        if (token.length > FCM_TOKEN_MAX_LENGTH) {
            return Mapped.Skipped("토큰 길이 초과")
        }

        val notes = mutableListOf<String>()
        val (createdAt, updatedAt) = timestamps(document, notes)
        val subscriptions = listOf("subscribedNoticeTopics", "subscribedMajorTopics", "subscribedMealTopics")
            .flatMap { document.strings(it) }
            .distinct()
            .mapNotNull { name ->
                topics[name]?.let { SubscriptionRow(newId(), it) } ?: null.also { notes += "모르는 구독 토픽: $name" }
            }

        return Mapped.Row(
            FcmTokenRow(
                id = newId(),
                token = token,
                deviceType = deviceType(document.string("deviceType"), notes),
                isActive = document.boolean("isActive", "active") ?: true,
                createdAt = createdAt,
                updatedAt = updatedAt,
                subscriptions = subscriptions,
            ),
            notes,
        )
    }

    fun user(document: LegacyDocument): Mapped<UserRow> {
        val email = document.string("email")?.takeIf { it.isNotBlank() } ?: return Mapped.Skipped("이메일 없음")
        val password = document.string("password")?.takeIf { it.isNotBlank() } ?: return Mapped.Skipped("비밀번호 없음")
        val nickname = document.string("nickname")?.takeIf { it.isNotBlank() } ?: return Mapped.Skipped("닉네임 없음")
        val role = document.string("role")?.takeIf { it in USER_ROLES } ?: return Mapped.Skipped("모르는 권한: ${document.string("role")}")
        if (email.length > USER_EMAIL_MAX_LENGTH || nickname.length > USER_NICKNAME_MAX_LENGTH || password.length > USER_PASSWORD_MAX_LENGTH) {
            return Mapped.Skipped("계정 값 길이 초과")
        }

        val notes = mutableListOf<String>()
        val (createdAt, updatedAt) = timestamps(document, notes)
        return Mapped.Row(UserRow(newId(), email, password, nickname, role, createdAt, updatedAt), notes)
    }

    fun tip(document: LegacyDocument): Mapped<TipRow> {
        val url = document.string("url")?.takeIf { it.isNotBlank() } ?: return Mapped.Skipped("링크 없음")
        if (url.length > TIP_URL_MAX_LENGTH) {
            return Mapped.Skipped("링크 길이 초과")
        }

        val notes = mutableListOf<String>()
        val (createdAt, updatedAt) = timestamps(document, notes)
        return Mapped.Row(
            TipRow(
                id = newId(),
                title = limit(document.string("title").orEmpty(), TIP_TITLE_MAX_LENGTH, "제목", notes),
                url = url,
                deviceType = deviceType(document.string("deviceType"), notes),
                createdAt = createdAt,
                updatedAt = updatedAt,
            ),
            notes,
        )
    }

    fun image(document: LegacyDocument): Mapped<ImageRow> {
        val imageUrl = document.string("imageUrl")?.takeIf { it.isNotBlank() } ?: return Mapped.Skipped("이미지 URL 없음")
        val serverName = document.string("serverName")?.takeIf { it.isNotBlank() } ?: return Mapped.Skipped("서버 파일명 없음")
        val extension = document.string("extension").orEmpty()
        val type = document.string("type")?.takeIf { it in IMAGE_TYPES } ?: return Mapped.Skipped("모르는 이미지 유형: ${document.string("type")}")
        if (imageUrl.length > IMAGE_URL_MAX_LENGTH || serverName.length > IMAGE_SERVER_NAME_MAX_LENGTH || extension.length > IMAGE_EXTENSION_MAX_LENGTH) {
            return Mapped.Skipped("이미지 파일 정보 길이 초과")
        }

        val notes = mutableListOf<String>()
        val (createdAt, updatedAt) = timestamps(document, notes)
        return Mapped.Row(
            ImageRow(
                id = newId(),
                imageUrl = imageUrl,
                originalName = limit(document.string("originalName").orEmpty(), IMAGE_ORIGINAL_NAME_MAX_LENGTH, "원본 파일명", notes),
                serverName = serverName,
                extension = extension,
                imageType = type,
                createdAt = createdAt,
                updatedAt = updatedAt,
            ),
            notes,
        )
    }

    fun report(document: LegacyDocument): Mapped<ReportRow> {
        val token = document.string("fcmToken")?.takeIf { it.isNotBlank() } ?: return Mapped.Skipped("토큰 없음")
        if (token.length > FCM_TOKEN_MAX_LENGTH) {
            return Mapped.Skipped("토큰 길이 초과")
        }

        val notes = mutableListOf<String>()
        val (createdAt, updatedAt) = timestamps(document, notes)
        return Mapped.Row(
            ReportRow(
                id = newId(),
                token = token,
                content = limit(document.string("content").orEmpty(), REPORT_CONTENT_MAX_LENGTH, "내용", notes),
                deviceName = limit(document.string("deviceName").orEmpty(), REPORT_DEVICE_NAME_MAX_LENGTH, "기기명", notes),
                version = limit(document.string("version").orEmpty(), REPORT_VERSION_MAX_LENGTH, "앱 버전", notes),
                createdAt = createdAt,
                updatedAt = updatedAt,
            ),
            notes,
        )
    }

    private fun timestamps(document: LegacyDocument, notes: MutableList<String>): Pair<LocalDateTime, LocalDateTime> {
        val createdAt = document.dateTime("createdAt") ?: now.also { notes += "생성 시각 없음 → 이관 시각" }
        return createdAt to (document.dateTime("updatedAt") ?: createdAt)
    }

    private fun deviceType(value: String?, notes: MutableList<String>): DeviceType =
        DeviceType.entries.firstOrNull { it.name == value }
            ?: DeviceType.UNKNOWN.also { if (value != null) notes += "모르는 기기 유형: $value" }

    private fun limit(value: String, maxLength: Int, field: String, notes: MutableList<String>): String {
        if (value.length <= maxLength) {
            return value
        }
        notes += "$field ${maxLength}자 초과로 자름"
        return value.take(maxLength)
    }

    /** api 모듈 엔티티의 컬럼 길이 · 값 (이 모듈은 api 에 의존하지 않는다). */
    companion object {
        private val USER_ROLES = setOf("ADMIN", "USER")
        private val IMAGE_TYPES = setOf("DEFAULT_IMAGE", "TIP_IMAGE")
        private const val USER_EMAIL_MAX_LENGTH = 200
        private const val USER_NICKNAME_MAX_LENGTH = 30
        private const val USER_PASSWORD_MAX_LENGTH = 100
        private const val TIP_TITLE_MAX_LENGTH = 200
        private const val TIP_URL_MAX_LENGTH = 1000
        private const val IMAGE_URL_MAX_LENGTH = 1000
        private const val IMAGE_ORIGINAL_NAME_MAX_LENGTH = 255
        private const val IMAGE_SERVER_NAME_MAX_LENGTH = 100
        private const val IMAGE_EXTENSION_MAX_LENGTH = 20
        private const val REPORT_CONTENT_MAX_LENGTH = 500
        private const val REPORT_DEVICE_NAME_MAX_LENGTH = 100
        private const val REPORT_VERSION_MAX_LENGTH = 50
    }

}
