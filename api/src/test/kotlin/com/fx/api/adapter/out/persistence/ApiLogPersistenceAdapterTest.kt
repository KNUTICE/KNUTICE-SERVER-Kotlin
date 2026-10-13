package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.ApiLogQueryRepository
import com.fx.api.config.querydsl.QuerydslConfig
import com.fx.api.domain.ApiLog
import com.fx.api.domain.ApiLogEntry
import com.fx.api.domain.ApiLogStatusGroup
import com.fx.api.fixture.TopicFixture
import com.fx.common.domain.DeviceType
import com.fx.persistence.MySqlContainerConfig
import com.fx.persistence.request.PagingRequest
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 실제 MySQL 에 API 로그를 묶어 저장하고 컬럼 값을 직접 읽어 엔티티 매핑이 Flyway 스키마와 맞는지 확인한다.
 * 원본 로그 목록은 행을 직접 넣고 조회한다.
 */
@DataJpaTest(properties = ["MYSQL_URL=unused", "MYSQL_DATABASE=unused", "MYSQL_USERNAME=unused", "MYSQL_PASSWORD=unused"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MySqlContainerConfig::class, QuerydslConfig::class, ApiLogQueryRepository::class, ApiLogPersistenceAdapter::class)
class ApiLogPersistenceAdapterTest @Autowired constructor(
    private val apiLogPersistenceAdapter: ApiLogPersistenceAdapter,
    private val entityManager: EntityManager,
    private val jdbcTemplate: JdbcTemplate,
) {

    private val requestedAt = LocalDateTime.of(2026, 10, 13, 23, 59, 59, 999_999_000)

    /** 원본 로그 목록을 조회하는 날 */
    private val logDate = LocalDate.of(2026, 10, 13)
    private val logTime = logDate.atTime(15, 0)

    @Test
    fun `묶음을 저장하면 모든 컬럼이 저장된다`() {
        apiLogPersistenceAdapter.saveAll(
            listOf(
                ApiLog(
                    requestedAt = requestedAt,
                    method = "GET",
                    urlPattern = "/open-api/v1/notices",
                    statusCode = 200,
                    durationMs = 35,
                    topic = TopicFixture.COMPUTER_SOFTWARE,
                    fcmTokenId = 893497211373609235L,
                ),
                ApiLog(
                    requestedAt = requestedAt.plusNanos(1_000),
                    method = "POST",
                    urlPattern = "/open-api/v1/reports",
                    statusCode = 500,
                    durationMs = 120,
                    topic = null,
                    fcmTokenId = null,
                ),
            )
        )
        entityManager.flush()

        val rows = jdbcTemplate.queryForList(
            """
            SELECT id, requested_at, method, url_pattern, status_code, duration_ms,
                   topic_code, topic_name, fcm_token_id, created_at, updated_at
            FROM api_log
            ORDER BY requested_at
            """.trimIndent()
        )
        assertThat(rows).hasSize(2)

        val withTopic = rows[0]
        assertThat(withTopic["id"]).isNotNull()
        assertThat(withTopic["requested_at"]).isEqualTo(requestedAt)
        assertThat(withTopic["method"]).isEqualTo("GET")
        assertThat(withTopic["url_pattern"]).isEqualTo("/open-api/v1/notices")
        assertThat(withTopic["status_code"]).isEqualTo(200)
        assertThat(withTopic["duration_ms"]).isEqualTo(35)
        assertThat(withTopic["topic_code"]).isEqualTo(300)
        assertThat(withTopic["topic_name"]).isEqualTo("COMPUTER_SOFTWARE")
        assertThat(withTopic["fcm_token_id"]).isEqualTo(893497211373609235L)
        assertThat(withTopic["created_at"]).isNotNull()
        assertThat(withTopic["updated_at"]).isNotNull()

        val withoutTopic = rows[1]
        assertThat(withoutTopic["method"]).isEqualTo("POST")
        assertThat(withoutTopic["status_code"]).isEqualTo(500)
        assertThat(withoutTopic["topic_code"]).isNull()
        assertThat(withoutTopic["topic_name"]).isNull()
        assertThat(withoutTopic["fcm_token_id"]).isNull()
    }

    /**
     * 10월 13일 로그를 최신순으로
     * - 5 : 15:00:03, 공지 목록, 200, iOS 기기
     * - 4 : 15:00:02, 공지 상세, 500, 등록되지 않은 토큰
     * - 3 : 15:00:02, 공지 목록, 200, Android 기기 (4 와 같은 시각)
     * - 2 : 15:00:02, 문의, 404 (4 와 같은 시각)
     * - 1 : 00:00:00, 공지 목록, 503
     * 다른 날: 10월 12일 23:59:59.999999 (6), 10월 14일 00:00 (7)
     */
    private fun insertApiLogs() {
        fcmToken(31, "ios-token", "iOS")
        fcmToken(32, "aos-token", "AOS")
        apiLog(1, logDate.atStartOfDay(), "GET", NOTICES, 503)
        apiLog(2, logTime.plusSeconds(2), "POST", REPORTS, 404)
        apiLog(3, logTime.plusSeconds(2), "GET", NOTICES, 200, topicCode = 1, topicName = "GENERAL_NEWS", fcmTokenId = 32)
        apiLog(4, logTime.plusSeconds(2), "GET", NOTICE, 500, fcmTokenId = 99)
        apiLog(5, logTime.plusSeconds(3), "GET", NOTICES, 200, fcmTokenId = 31)
        apiLog(6, logDate.atStartOfDay().minusNanos(1_000), "GET", NOTICES, 200)
        apiLog(7, logDate.plusDays(1).atStartOfDay(), "GET", NOTICES, 200)
    }

    @Test
    fun `원본 로그를 하루치만 요청 시각 · ID 내림차순으로 읽고 기기 종류를 붙인다`() {
        insertApiLogs()

        val entries = apiLogPersistenceAdapter.findApiLogs(logDate, null, null, PagingRequest(size = 10).toPageable()).content

        assertThat(idsOf(entries)).containsExactly(5L, 4L, 3L, 2L, 1L)
        assertThat(entries[0].deviceType).isEqualTo(DeviceType.iOS)
        assertThat(entries[1].deviceType).isNull()
        assertThat(entries[2]).isEqualTo(
            ApiLogEntry(3, logTime.plusSeconds(2), "GET", NOTICES, 200, 8, 1, "GENERAL_NEWS", DeviceType.AOS)
        )
    }

    @Test
    fun `페이지 단위로 읽고 전체 건수를 센다`() {
        insertApiLogs()

        val first = apiLogPersistenceAdapter.findApiLogs(logDate, null, null, PagingRequest(page = 1, size = 2).toPageable())
        assertThat(idsOf(first.content)).containsExactly(5L, 4L)
        assertThat(first.totalElements).isEqualTo(5)
        assertThat(first.totalPages).isEqualTo(3)

        val last = apiLogPersistenceAdapter.findApiLogs(logDate, null, null, PagingRequest(page = 3, size = 2).toPageable())
        assertThat(idsOf(last.content)).containsExactly(1L)
        assertThat(last.hasNext()).isFalse()
    }

    @Test
    fun `URL 패턴 · 상태 코드 묶음으로 거른다`() {
        insertApiLogs()

        fun ids(urlPattern: String?, statusGroup: ApiLogStatusGroup?): List<Long> =
            idsOf(apiLogPersistenceAdapter.findApiLogs(logDate, urlPattern, statusGroup, PagingRequest().toPageable()).content)

        assertThat(ids(NOTICES, null)).containsExactly(5L, 3L, 1L)
        assertThat(ids(null, ApiLogStatusGroup.SERVER_ERROR)).containsExactly(4L, 1L)
        assertThat(ids(null, ApiLogStatusGroup.CLIENT_ERROR)).containsExactly(2L)
        assertThat(ids(NOTICES, ApiLogStatusGroup.SERVER_ERROR)).containsExactly(1L)
        assertThat(ids(" ", null)).hasSize(5)
    }

    private fun idsOf(entries: List<ApiLogEntry>): List<Long> =
        entries.map {
            it.id
        }

    private fun apiLog(
        id: Long,
        requestedAt: LocalDateTime,
        method: String,
        urlPattern: String,
        statusCode: Int,
        topicCode: Int? = null,
        topicName: String? = null,
        fcmTokenId: Long? = null,
    ) {
        jdbcTemplate.update(
            """
            INSERT INTO api_log
                (id, requested_at, method, url_pattern, status_code, duration_ms, topic_code, topic_name, fcm_token_id, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            id, requestedAt, method, urlPattern, statusCode, 8, topicCode, topicName, fcmTokenId, requestedAt, requestedAt,
        )
    }

    private fun fcmToken(id: Long, token: String, deviceType: String) {
        jdbcTemplate.update(
            """
            INSERT INTO fcm_token (id, token, device_type, is_active, language, created_at, updated_at)
            VALUES (?, ?, ?, TRUE, 'ko', ?, ?)
            """,
            id, token, deviceType, logTime, logTime,
        )
    }

    companion object {
        private const val NOTICES = "/open-api/v1/notices"
        private const val NOTICE = "/open-api/v1/notices/{nttId}"
        private const val REPORTS = "/open-api/v1/reports"
    }

}
