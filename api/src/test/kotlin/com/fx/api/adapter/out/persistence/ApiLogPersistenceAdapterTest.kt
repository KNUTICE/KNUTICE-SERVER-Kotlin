package com.fx.api.adapter.out.persistence

import com.fx.api.domain.ApiLog
import com.fx.api.fixture.TopicFixture
import com.fx.persistence.MySqlContainerConfig
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDateTime

/** 실제 MySQL 에 API 로그를 묶어 저장하고 컬럼 값을 직접 읽어 엔티티 매핑이 Flyway 스키마와 맞는지 확인한다. */
@DataJpaTest(properties = ["MYSQL_URL=unused", "MYSQL_DATABASE=unused", "MYSQL_USERNAME=unused", "MYSQL_PASSWORD=unused"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MySqlContainerConfig::class, ApiLogPersistenceAdapter::class)
class ApiLogPersistenceAdapterTest @Autowired constructor(
    private val apiLogPersistenceAdapter: ApiLogPersistenceAdapter,
    private val entityManager: EntityManager,
    private val jdbcTemplate: JdbcTemplate,
) {

    private val requestedAt = LocalDateTime.of(2026, 10, 13, 23, 59, 59, 999_999_000)

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

}
