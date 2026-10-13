package com.fx.api.adapter.out.persistence

import com.fx.api.domain.DailyEndpointStatistics
import com.fx.api.domain.DailyTopicStatistics
import com.fx.api.domain.DeviceCounts
import com.fx.api.domain.EndpointStatistics
import com.fx.api.domain.RequestCounts
import com.fx.api.domain.StatisticsPeriod
import com.fx.api.domain.TopicRequestCount
import com.fx.persistence.MySqlContainerConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDate
import java.time.LocalDateTime

/** crawler 의 일일 통계 Job 이 남기는 형태로 통계 행을 넣고 실제 MySQL 에서 조회한다. */
@DataJpaTest(properties = ["MYSQL_URL=unused", "MYSQL_DATABASE=unused", "MYSQL_USERNAME=unused", "MYSQL_PASSWORD=unused"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MySqlContainerConfig::class, StatisticsPersistenceAdapter::class)
class StatisticsPersistenceAdapterTest @Autowired constructor(
    private val statisticsPersistenceAdapter: StatisticsPersistenceAdapter,
    private val jdbcTemplate: JdbcTemplate,
) {

    private val oct9 = LocalDate.of(2026, 10, 9)
    private val oct10 = LocalDate.of(2026, 10, 10)
    private val oct11 = LocalDate.of(2026, 10, 11)
    private val oct12 = LocalDate.of(2026, 10, 12)
    private val aggregatedAt = LocalDateTime.of(2026, 10, 12, 0, 10, 2, 418_233_000)

    /** 10월 12일은 아직 집계되지 않았다. 10월 10일은 Job 이 다음 날 실행되지 않아 토큰 현황이 없다. */
    private val period = StatisticsPeriod(oct10, oct12)

    @BeforeEach
    fun setUp() {
        dailyStatistics(1, oct9, requestCount = 999, snapshot = listOf(1, 1, 1, 1))
        dailyStatistics(2, oct10, requestCount = 110, snapshot = null)
        dailyStatistics(3, oct11, requestCount = 60, snapshot = listOf(605, 507, 402, 331))

        endpointStatistics(11, oct9, "GET", NOTICES, requestCount = 1000, clientErrors = 0, serverErrors = 0, totalDuration = 1000, maxDuration = 1, devices = 1)
        endpointStatistics(12, oct10, "GET", NOTICES, requestCount = 100, clientErrors = 2, serverErrors = 0, totalDuration = 800, maxDuration = 50, devices = 30)
        endpointStatistics(13, oct11, "GET", NOTICES, requestCount = 50, clientErrors = 1, serverErrors = 1, totalDuration = 450, maxDuration = 90, devices = 20)
        endpointStatistics(14, oct11, "GET", NOTICE, requestCount = 10, clientErrors = 0, serverErrors = 0, totalDuration = 40, maxDuration = 7, devices = 5)

        topicStatistics(21, oct9, 1, "GENERAL_NEWS", 100)
        topicStatistics(22, oct10, 1, "GENERAL_NEWS", 5)
        topicStatistics(23, oct11, 1, "GENERAL_NEWS", 7)
        topicStatistics(24, oct11, 301, "COMPUTER_SCIENCE", 3)
    }

    @Test
    fun `일별 요약은 기간 안의 집계된 날만 날짜순으로 읽는다`() {
        val days = statisticsPersistenceAdapter.findDailyStatistics(period)

        assertThat(days.map {
            it.statisticsDate
        }).containsExactly(oct10, oct11)
        val oct11Day = days[1]
        assertThat(oct11Day.requestCount).isEqualTo(60)
        assertThat(oct11Day.deviceCount).isEqualTo(21)
        assertThat(oct11Day.iosDeviceCount).isEqualTo(11)
        assertThat(oct11Day.aosDeviceCount).isEqualTo(9)
        assertThat(oct11Day.noticeCount).isEqualTo(4)
        assertThat(oct11Day.summaryCompletedCount).isEqualTo(3)
        assertThat(oct11Day.summaryFailedCount).isEqualTo(1)
        assertThat(oct11Day.newTokenCount).isEqualTo(6)
        assertThat(oct11Day.aggregatedAt).isEqualTo(aggregatedAt)
    }

    @Test
    fun `토큰 현황은 기록하지 못한 날이면 비어 있다`() {
        val days = statisticsPersistenceAdapter.findDailyTokenStatistics(period)

        assertThat(days.map {
            it.statisticsDate
        }).containsExactly(oct10, oct11)
        assertThat(days[0].active).isNull()
        assertThat(days[0].inactive).isNull()
        assertThat(days[1].newTokenCount).isEqualTo(6)
        assertThat(days[1].active).isEqualTo(DeviceCounts(1112, 605, 507))
        assertThat(days[1].inactive).isEqualTo(DeviceCounts(733, 402, 331))
    }

    @Test
    fun `엔드포인트별로 기간 합계를 요청 수 내림차순으로 읽는다`() {
        assertThat(statisticsPersistenceAdapter.findEndpointStatistics(period)).containsExactly(
            EndpointStatistics("GET", NOTICES, RequestCounts(150, 3, 1, 1250, 90)),
            EndpointStatistics("GET", NOTICE, RequestCounts(10, 0, 0, 40, 7)),
        )
    }

    @Test
    fun `엔드포인트 일별 추이는 집계된 날에 요청이 없으면 0 이고 집계되지 않은 날은 없다`() {
        assertThat(statisticsPersistenceAdapter.findDailyEndpointStatistics("GET", NOTICE, period)).containsExactly(
            DailyEndpointStatistics(oct10, RequestCounts(0, 0, 0, 0, null), 0),
            DailyEndpointStatistics(oct11, RequestCounts(10, 0, 0, 40, 7), 5),
        )
        val unknownEndpoint =
            statisticsPersistenceAdapter.findDailyEndpointStatistics("POST", NOTICE, period)
        assertThat(unknownEndpoint).containsExactly(
            DailyEndpointStatistics(oct10, RequestCounts(0, 0, 0, 0, null), 0),
            DailyEndpointStatistics(oct11, RequestCounts(0, 0, 0, 0, null), 0),
        )
    }

    @Test
    fun `토픽별 조회 수는 기간 합계이고 조회 기록이 있는 토픽만 읽는다`() {
        assertThat(statisticsPersistenceAdapter.findTopicRequestCounts(period)).containsExactlyInAnyOrder(
            TopicRequestCount(1, "GENERAL_NEWS", 12),
            TopicRequestCount(301, "COMPUTER_SCIENCE", 3),
        )
    }

    @Test
    fun `토픽 일별 조회 수는 집계된 날에 기록이 없으면 0 이다`() {
        assertThat(statisticsPersistenceAdapter.findDailyTopicStatistics(301, period)).containsExactly(
            DailyTopicStatistics(oct10, 0),
            DailyTopicStatistics(oct11, 3),
        )
    }

    @Test
    fun `구독자 수와 활성 토큰 수는 활성 토큰만 센다`() {
        fcmToken(31, "ios-active", "iOS", active = true)
        fcmToken(32, "aos-active", "AOS", active = true)
        fcmToken(33, "ios-inactive", "iOS", active = false)
        fcmToken(34, "unknown-active", "UNKNOWN", active = true)
        subscription(41, fcmTokenId = 31, topicCode = 1, topicName = "GENERAL_NEWS")
        subscription(42, fcmTokenId = 32, topicCode = 1, topicName = "GENERAL_NEWS")
        subscription(43, fcmTokenId = 33, topicCode = 1, topicName = "GENERAL_NEWS")
        subscription(44, fcmTokenId = 31, topicCode = 900, topicName = "STUDENT_CAFETERIA")
        subscription(45, fcmTokenId = 34, topicCode = 900, topicName = "STUDENT_CAFETERIA")
        subscription(46, fcmTokenId = 33, topicCode = 2, topicName = "SCHOLARSHIP_NEWS")

        assertThat(statisticsPersistenceAdapter.countSubscribersByTopic()).containsExactlyInAnyOrderEntriesOf(
            mapOf(
                1 to DeviceCounts(2, 1, 1),
                900 to DeviceCounts(2, 1, 0),
            )
        )
        assertThat(statisticsPersistenceAdapter.countActiveTokens()).isEqualTo(DeviceCounts(3, 1, 1))
    }

    @Test
    fun `활성 토큰이 없으면 모두 0 이다`() {
        assertThat(statisticsPersistenceAdapter.countSubscribersByTopic()).isEmpty()
        assertThat(statisticsPersistenceAdapter.countActiveTokens()).isEqualTo(DeviceCounts.ZERO)
    }

    /** [snapshot] 은 활성 iOS · 활성 Android · 비활성 iOS · 비활성 Android 순 */
    private fun dailyStatistics(id: Long, date: LocalDate, requestCount: Long, snapshot: List<Int>?) {
        jdbcTemplate.update(
            """
            INSERT INTO daily_statistics
                (id, statistics_date, request_count, device_count, ios_device_count, aos_device_count,
                 notice_count, summary_completed_count, summary_failed_count, new_token_count,
                 active_ios_token_count, active_aos_token_count, inactive_ios_token_count, inactive_aos_token_count,
                 created_at, updated_at)
            VALUES (?, ?, ?, 21, 11, 9, 4, 3, 1, 6, ?, ?, ?, ?, ?, ?)
            """,
            id, date, requestCount,
            snapshot?.get(0), snapshot?.get(1), snapshot?.get(2), snapshot?.get(3),
            aggregatedAt, aggregatedAt,
        )
    }

    private fun endpointStatistics(
        id: Long,
        date: LocalDate,
        method: String,
        urlPattern: String,
        requestCount: Long,
        clientErrors: Long,
        serverErrors: Long,
        totalDuration: Long,
        maxDuration: Int,
        devices: Int,
    ) {
        jdbcTemplate.update(
            """
            INSERT INTO daily_api_log_statistics
                (id, statistics_date, method, url_pattern, request_count, client_error_count, server_error_count,
                 total_duration_ms, max_duration_ms, device_count, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            id, date, method, urlPattern, requestCount, clientErrors, serverErrors, totalDuration, maxDuration, devices,
            aggregatedAt, aggregatedAt,
        )
    }

    private fun topicStatistics(id: Long, date: LocalDate, topicCode: Int, topicName: String, requestCount: Long) {
        jdbcTemplate.update(
            """
            INSERT INTO daily_topic_statistics (id, statistics_date, topic_code, topic_name, request_count, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """,
            id, date, topicCode, topicName, requestCount, aggregatedAt, aggregatedAt,
        )
    }

    private fun fcmToken(id: Long, token: String, deviceType: String, active: Boolean) {
        jdbcTemplate.update(
            """
            INSERT INTO fcm_token (id, token, device_type, is_active, language, created_at, updated_at)
            VALUES (?, ?, ?, ?, 'ko', ?, ?)
            """,
            id, token, deviceType, active, aggregatedAt, aggregatedAt,
        )
    }

    private fun subscription(id: Long, fcmTokenId: Long, topicCode: Int, topicName: String) {
        jdbcTemplate.update(
            """
            INSERT INTO fcm_token_subscription (id, fcm_token_id, topic_code, topic_name, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """,
            id, fcmTokenId, topicCode, topicName, aggregatedAt, aggregatedAt,
        )
    }

    companion object {
        private const val NOTICES = "/open-api/v1/notices"
        private const val NOTICE = "/open-api/v1/notices/{nttId}"
    }

}
