package com.fx.migration.target

import com.fx.common.domain.DeviceType
import com.fx.common.domain.i18n.Language
import com.fx.common.domain.notice.NotificationStatus
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 이관 대상 MySQL 테이블에 JDBC 배치 INSERT 로 쓴다.
 * 레거시 생성 · 수정 시각을 그대로 넣어야 하므로 JPA Auditing 을 거치지 않는다.
 * id 는 호출하는 쪽이 넘긴 순서대로 발급한 TSID 라, 레거시 생성 순서대로 넣으면 id 순서도 그대로 유지된다.
 */
@Component
class TargetWriter(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) {

    /** 이미 행이 있는 이관 대상 테이블. 이관은 빈 테이블에만 한다. */
    fun nonEmptyTables(): List<String> =
        TARGET_TABLES.filter { count(it) > 0 }

    fun count(table: String): Long {
        require(table in TARGET_TABLES) { "이관 대상 테이블이 아닙니다: $table" }
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM $table", emptyMap<String, Any>(), Long::class.java) ?: 0
    }

    /** 레거시 enum 이름(= `topic.name`) → 토픽. 삭제된 토픽도 포함한다. */
    fun loadTopics(): Map<String, TopicRef> =
        jdbcTemplate.query("SELECT code, name FROM topic", emptyMap<String, Any>()) { rs, _ ->
            TopicRef(rs.getInt("code"), rs.getString("name"))
        }.associateBy { it.name }

    fun insertNotices(rows: List<NoticeRow>) {
        batch(
            """
            INSERT INTO notice (id, ntt_id, topic_code, topic_name, title, department, content_url, content_image_url,
                                registration_date, is_attachment, notification_status, notified_at, summary_status,
                                summary_attempt_count, created_at, updated_at)
            VALUES (:id, :nttId, :topicCode, :topicName, :title, :department, :contentUrl, :contentImageUrl,
                    :registrationDate, :isAttachment, :notificationStatus, :notifiedAt, :summaryStatus,
                    0, :createdAt, :updatedAt)
            """,
            rows.map {
                params(it.id, it.createdAt, it.updatedAt)
                    .addValue("nttId", it.nttId)
                    .addValue("topicCode", it.topic.code)
                    .addValue("topicName", it.topic.name)
                    .addValue("title", it.title)
                    .addValue("department", it.department)
                    .addValue("contentUrl", it.contentUrl)
                    .addValue("contentImageUrl", it.contentImageUrl)
                    .addValue("registrationDate", it.registrationDate)
                    .addValue("isAttachment", it.isAttachment)
                    .addValue("notificationStatus", NotificationStatus.SENT.name)
                    .addValue("notifiedAt", it.notifiedAt)
                    .addValue("summaryStatus", it.summaryStatus.name)
            },
        )
        batch(
            """
            INSERT INTO notice_content (id, notice_id, content, content_summary, created_at, updated_at)
            VALUES (:id, :noticeId, :content, :contentSummary, :createdAt, :updatedAt)
            """,
            rows.map {
                params(it.contentId, it.createdAt, it.updatedAt)
                    .addValue("noticeId", it.id)
                    .addValue("content", it.content)
                    .addValue("contentSummary", it.contentSummary)
            },
        )
    }

    /** 토큰과 구독을 함께 넣는다. 구독 시각은 레거시에 없으므로 토큰 생성 시각을 쓴다. */
    fun insertFcmTokens(rows: List<FcmTokenRow>) {
        batch(
            """
            INSERT INTO fcm_token (id, token, device_type, is_active, language, created_at, updated_at)
            VALUES (:id, :token, :deviceType, :isActive, :language, :createdAt, :updatedAt)
            """,
            rows.map {
                params(it.id, it.createdAt, it.updatedAt)
                    .addValue("token", it.token)
                    .addValue("deviceType", it.deviceType.name)
                    .addValue("isActive", it.isActive)
                    .addValue("language", Language.DEFAULT.code)
            },
        )
        batch(
            """
            INSERT INTO fcm_token_subscription (id, fcm_token_id, topic_code, topic_name, created_at, updated_at)
            VALUES (:id, :fcmTokenId, :topicCode, :topicName, :createdAt, :updatedAt)
            """,
            rows.flatMap { token ->
                token.subscriptions.map {
                    params(it.id, token.createdAt, token.createdAt)
                        .addValue("fcmTokenId", token.id)
                        .addValue("topicCode", it.topic.code)
                        .addValue("topicName", it.topic.name)
                }
            },
        )
    }

    fun findTokenIds(tokens: Collection<String>): Map<String, Long> {
        if (tokens.isEmpty()) {
            return emptyMap()
        }
        return jdbcTemplate.query(
            "SELECT id, token FROM fcm_token WHERE token IN (:tokens)",
            mapOf("tokens" to tokens),
        ) { rs, _ -> rs.getString("token") to rs.getLong("id") }.toMap()
    }

    fun insertUsers(rows: List<UserRow>) {
        batch(
            """
            INSERT INTO users (id, email, password, nickname, role, created_at, updated_at)
            VALUES (:id, :email, :password, :nickname, :role, :createdAt, :updatedAt)
            """,
            rows.map {
                params(it.id, it.createdAt, it.updatedAt)
                    .addValue("email", it.email)
                    .addValue("password", it.password)
                    .addValue("nickname", it.nickname)
                    .addValue("role", it.role)
            },
        )
    }

    fun insertTips(rows: List<TipRow>) {
        batch(
            """
            INSERT INTO tip (id, title, url, device_type, created_at, updated_at)
            VALUES (:id, :title, :url, :deviceType, :createdAt, :updatedAt)
            """,
            rows.map {
                params(it.id, it.createdAt, it.updatedAt)
                    .addValue("title", it.title)
                    .addValue("url", it.url)
                    .addValue("deviceType", it.deviceType.name)
            },
        )
    }

    fun insertImages(rows: List<ImageRow>) {
        batch(
            """
            INSERT INTO image (id, image_url, original_name, server_name, extension, image_type, created_at, updated_at)
            VALUES (:id, :imageUrl, :originalName, :serverName, :extension, :imageType, :createdAt, :updatedAt)
            """,
            rows.map {
                params(it.id, it.createdAt, it.updatedAt)
                    .addValue("imageUrl", it.imageUrl)
                    .addValue("originalName", it.originalName)
                    .addValue("serverName", it.serverName)
                    .addValue("extension", it.extension)
                    .addValue("imageType", it.imageType)
            },
        )
    }

    /** @param tokenIds 토큰 문자열 → `fcm_token.id`. [rows] 의 토큰이 모두 있어야 한다 */
    fun insertReports(rows: List<ReportRow>, tokenIds: Map<String, Long>) {
        batch(
            """
            INSERT INTO report (id, fcm_token_id, content, device_name, version, created_at, updated_at)
            VALUES (:id, :fcmTokenId, :content, :deviceName, :version, :createdAt, :updatedAt)
            """,
            rows.map {
                params(it.id, it.createdAt, it.updatedAt)
                    .addValue("fcmTokenId", tokenIds.getValue(it.token))
                    .addValue("content", it.content)
                    .addValue("deviceName", it.deviceName)
                    .addValue("version", it.version)
            },
        )
    }

    /** 문의만 남고 토큰이 지워진 경우 문의를 잃지 않도록 비활성 토큰으로 만든다 (발송 대상이 아니다). */
    fun insertInactiveTokens(tokens: Map<String, LocalDateTime>, newId: () -> Long): Map<String, Long> {
        val rows = tokens.map { (token, createdAt) ->
            FcmTokenRow(newId(), token, DeviceType.UNKNOWN, isActive = false, createdAt = createdAt, updatedAt = createdAt, subscriptions = emptyList())
        }
        insertFcmTokens(rows)
        return rows.associate { it.token to it.id }
    }

    private fun params(id: Long, createdAt: LocalDateTime, updatedAt: LocalDateTime): MapSqlParameterSource =
        MapSqlParameterSource()
            .addValue("id", id)
            .addValue("createdAt", createdAt)
            .addValue("updatedAt", updatedAt)

    private fun batch(sql: String, params: List<MapSqlParameterSource>) {
        if (params.isNotEmpty()) {
            jdbcTemplate.batchUpdate(sql.trimIndent(), params.toTypedArray())
        }
    }

    companion object {
        val TARGET_TABLES = listOf(
            "notice", "notice_content", "fcm_token", "fcm_token_subscription", "users", "tip", "image", "report",
        )
    }

}
