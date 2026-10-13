package com.fx.crawler.adapter.`in`.batch.job

import com.fx.common.domain.DeviceType
import com.fx.common.domain.notice.SummaryStatus
import com.fx.common.domain.statistics.StatisticsTime
import com.fx.crawler.domain.statistics.ApiLogStatistics
import com.fx.crawler.domain.statistics.DailyTotals
import com.fx.crawler.domain.statistics.TokenSnapshot
import com.fx.crawler.domain.statistics.TopicStatistics
import com.fx.crawler.support.CrawlerIntegrationTest
import io.hypersistence.tsid.TSID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.JobExecution
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import java.sql.ResultSet
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class DailyStatisticsJobTest : CrawlerIntegrationTest() {

    @Autowired @Qualifier("dailyStatisticsJob") lateinit var dailyStatisticsJob: Job
    @Autowired lateinit var clock: Clock

    private lateinit var today: LocalDate
    private lateinit var yesterday: LocalDate
    private lateinit var twoDaysAgo: LocalDate
    private lateinit var threeDaysAgo: LocalDate

    @BeforeEach
    fun setUpDates() {
        today = StatisticsTime.today(clock)
        yesterday = today.minusDays(1)
        twoDaysAgo = today.minusDays(2)
        threeDaysAgo = today.minusDays(3)
    }

    @Test
    fun `최근 일수의 로그 · 공지 · 토큰을 KST 날짜별로 집계한다`() {
        givenSampleData()

        val execution = run(dailyStatisticsJob, PARAMETERS)

        assertThat(execution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(apiLogStatistics()).containsExactlyInAnyOrder(
            // 전날 23:59:59.999999 의 로그는 전날에 들어간다
            twoDaysAgo to ApiLogStatistics("GET", TOPICS, 1, 0, 0, 40, 40, 1),
            // 그날 00:00 의 로그는 그날에 들어간다
            yesterday to ApiLogStatistics("GET", TOPICS, 2, 0, 1, 160, 100, 2),
            yesterday to ApiLogStatistics("GET", NOTICES_V1, 2, 1, 0, 35, 30, 2),
            // 토큰 헤더가 없던 요청은 기기 수에 넣지 않는다
            yesterday to ApiLogStatistics("GET", NOTICES_V2, 2, 0, 0, 35, 20, 1),
            yesterday to ApiLogStatistics("PATCH", LANGUAGE, 1, 1, 0, 7, 7, 1),
        )
        // 목록이 비어 4xx 로 끝난 조회도 토픽 조회 수에 넣는다
        assertThat(topicStatistics()).containsExactlyInAnyOrder(
            yesterday to TopicStatistics(1, "GENERAL_NEWS", 3),
            yesterday to TopicStatistics(300, "COMPUTER_SOFTWARE", 1),
        )
        val daily = dailyStatistics()
        assertThat(daily.keys).containsExactlyInAnyOrder(threeDaysAgo, twoDaysAgo, yesterday)
        // 기기를 알 수 없는 토큰은 전체 기기 수에만 들어간다
        assertThat(daily.getValue(yesterday).totals).isEqualTo(DailyTotals(7, 4, 1, 2, 3, 1, 1, 2))
        assertThat(daily.getValue(twoDaysAgo).totals).isEqualTo(DailyTotals(1, 1, 1, 0, 1, 1, 0, 1))
        assertThat(daily.getValue(threeDaysAgo).totals).isEqualTo(DailyTotals(0, 0, 0, 0, 0, 0, 0, 1))
        // 날짜별 쓴 행 수 : 어제 4 + 2 + 1, 그저께 1 + 0 + 1, 사흘 전 0 + 0 + 1
        assertThat(step(execution, "dailyStatisticsAggregateStep").writeCount).isEqualTo(10)
    }

    @Test
    fun `같은 파라미터로 다시 실행해도 통계 값과 행 수가 같고, 늦게 들어온 로그는 값에만 반영된다`() {
        givenSampleData()
        run(dailyStatisticsJob, PARAMETERS)
        val first = snapshotAll()
        val firstRows = rowKeys()

        assertThat(run(dailyStatisticsJob, PARAMETERS).status).isEqualTo(BatchStatus.COMPLETED)

        assertThat(snapshotAll()).isEqualTo(first)
        assertThat(rowKeys()).isEqualTo(firstRows)

        // 어제 로그가 늦게 저장된 뒤 다시 집계한다
        insertApiLog(yesterday.atTime(15, 0), NOTICES_V1, durationMs = 90, topic = GENERAL_NEWS, fcmTokenId = IOS_ACTIVE)

        assertThat(run(dailyStatisticsJob, PARAMETERS).status).isEqualTo(BatchStatus.COMPLETED)

        assertThat(rowKeys()).isEqualTo(firstRows)
        assertThat(apiLogStatistics()).contains(yesterday to ApiLogStatistics("GET", NOTICES_V1, 3, 1, 0, 125, 90, 2))
        assertThat(topicStatistics()).contains(yesterday to TopicStatistics(1, "GENERAL_NEWS", 4))
        assertThat(dailyStatistics().getValue(yesterday).totals.requestCount).isEqualTo(8)
    }

    @Test
    fun `토큰 현황은 어제 날짜에만 기록하고 지난 날짜에 기록해 둔 값은 바꾸지 않는다`() {
        givenSampleData()
        // 그저께를 어제로 집계했던 실행이 남긴 토큰 현황
        jdbcTemplate.update(
            """
            INSERT INTO daily_statistics (id, statistics_date, request_count, device_count, ios_device_count, aos_device_count,
                notice_count, summary_completed_count, summary_failed_count, new_token_count,
                active_ios_token_count, active_aos_token_count, inactive_ios_token_count, inactive_aos_token_count, created_at, updated_at)
            VALUES (?, ?, 999, 0, 0, 0, 0, 0, 0, 0, 5, 6, 7, 8, ?, ?)
            """,
            TSID.fast().toLong(), twoDaysAgo, twoDaysAgo.atTime(0, 10), twoDaysAgo.atTime(0, 10),
        )

        run(dailyStatisticsJob, PARAMETERS)

        val daily = dailyStatistics()
        // 활성 iOS 1 · 활성 Android 1 · 비활성 iOS 1 · 비활성 Android 1 (기기를 알 수 없는 토큰은 세지 않는다)
        assertThat(daily.getValue(yesterday).snapshot).isEqualTo(TokenSnapshot(1, 1, 1, 1))
        assertThat(daily.getValue(twoDaysAgo).snapshot).isEqualTo(TokenSnapshot(5, 6, 7, 8))
        assertThat(daily.getValue(twoDaysAgo).totals.requestCount).isEqualTo(1)
        assertThat(daily.getValue(threeDaysAgo).snapshot).isNull()
    }

    @Test
    fun `보관 기간이 지난 로그만 지우고, 한 번에 지우는 수보다 많아도 모두 지운다`() {
        val cutoff = today.minusDays(30).atStartOfDay()
        val expired = DailyStatisticsJobConfig.DELETE_LIMIT + 1
        insertApiLogs(List(expired) {
            cutoff.minusHours(1)
        })
        insertApiLog(cutoff.minusNanos(1_000), TOPICS)
        insertApiLog(cutoff, TOPICS)
        insertApiLog(yesterday.atTime(9, 0), TOPICS)

        val execution = run(dailyStatisticsJob, mapOf("lookbackDays" to "1", "apiLogRetentionDays" to "30"))

        assertThat(execution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jdbcTemplate.queryForList("SELECT requested_at FROM api_log", LocalDateTime::class.java))
            .containsExactlyInAnyOrder(cutoff, yesterday.atTime(9, 0))
        assertThat(step(execution, "apiLogPurgeStep").writeCount).isEqualTo(expired + 1L)
    }

    @Test
    fun `다시 집계할 일수가 보관 일수보다 작지 않으면 집계도 삭제도 하지 않고 실패한다`() {
        insertApiLog(yesterday.atTime(9, 0), TOPICS)
        insertApiLog(today.minusDays(40).atTime(9, 0), TOPICS)

        val execution = run(dailyStatisticsJob, mapOf("lookbackDays" to "30", "apiLogRetentionDays" to "30"))

        assertThat(execution.status).isEqualTo(BatchStatus.FAILED)
        assertThat(count("daily_statistics")).isZero()
        assertThat(count("api_log")).isEqualTo(2)
    }

    @Test
    fun `파라미터가 없으면 실패한다`() {
        insertApiLog(yesterday.atTime(9, 0), TOPICS)

        assertThat(run(dailyStatisticsJob).status).isEqualTo(BatchStatus.FAILED)
        assertThat(run(dailyStatisticsJob, mapOf("lookbackDays" to "3")).status).isEqualTo(BatchStatus.FAILED)
        assertThat(run(dailyStatisticsJob, mapOf("apiLogRetentionDays" to "30")).status).isEqualTo(BatchStatus.FAILED)
        assertThat(count("daily_statistics")).isZero()
    }

    /**
     * 사흘 전 : 토큰 1
     * 그저께 : 로그 1 (23:59:59.999999), 공지 1, 토큰 1
     * 어제 : 로그 7 (00:00 포함), 공지 3, 토큰 2 (23:59:59.999999 포함)
     * 오늘 : 로그 · 공지 · 토큰 1 씩 (00:00) — 집계 대상이 아니다
     */
    private fun givenSampleData() {
        insertFcmToken(IOS_ACTIVE, DeviceType.iOS, active = true, yesterday.atTime(10, 0))
        insertFcmToken(AOS_ACTIVE, DeviceType.AOS, active = true, twoDaysAgo.atTime(9, 0))
        insertFcmToken(AOS_INACTIVE, DeviceType.AOS, active = false, yesterday.atTime(LAST_MOMENT))
        insertFcmToken(IOS_INACTIVE, DeviceType.iOS, active = false, threeDaysAgo.atTime(12, 0))
        insertFcmToken(UNKNOWN_ACTIVE, DeviceType.UNKNOWN, active = true, today.atStartOfDay())

        insertApiLog(twoDaysAgo.atTime(LAST_MOMENT), TOPICS, durationMs = 40, fcmTokenId = IOS_ACTIVE)
        insertApiLog(yesterday.atStartOfDay(), TOPICS, durationMs = 60, fcmTokenId = IOS_ACTIVE)
        insertApiLog(yesterday.atTime(12, 0), TOPICS, statusCode = 503, durationMs = 100, fcmTokenId = AOS_ACTIVE)
        insertApiLog(yesterday.atTime(9, 0), NOTICES_V1, durationMs = 30, topic = GENERAL_NEWS, fcmTokenId = IOS_ACTIVE)
        insertApiLog(yesterday.atTime(9, 30), NOTICES_V1, statusCode = 404, durationMs = 5, topic = GENERAL_NEWS, fcmTokenId = AOS_ACTIVE)
        insertApiLog(yesterday.atTime(10, 0), NOTICES_V2, durationMs = 20, topic = GENERAL_NEWS)
        insertApiLog(yesterday.atTime(11, 0), NOTICES_V2, durationMs = 15, topic = COMPUTER_SOFTWARE, fcmTokenId = AOS_INACTIVE)
        insertApiLog(yesterday.atTime(13, 0), LANGUAGE, method = "PATCH", statusCode = 404, durationMs = 7, fcmTokenId = UNKNOWN_ACTIVE)
        insertApiLog(today.atStartOfDay(), TOPICS, durationMs = 10, fcmTokenId = IOS_ACTIVE)

        insertNotice(1, SummaryStatus.COMPLETED, twoDaysAgo.atTime(16, 10))
        insertNotice(2, SummaryStatus.COMPLETED, yesterday.atTime(9, 0))
        insertNotice(3, SummaryStatus.FAILED, yesterday.atTime(16, 10))
        insertNotice(4, SummaryStatus.PENDING, yesterday.atTime(LAST_MOMENT))
        insertNotice(5, SummaryStatus.COMPLETED, today.atStartOfDay())
    }

    private fun insertApiLog(
        requestedAt: LocalDateTime,
        urlPattern: String,
        method: String = "GET",
        statusCode: Int = 200,
        durationMs: Int = 10,
        topic: Pair<Int, String>? = null,
        fcmTokenId: Long? = null,
    ) {
        jdbcTemplate.update(
            """
            INSERT INTO api_log (id, requested_at, method, url_pattern, status_code, duration_ms, topic_code, topic_name, fcm_token_id, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            TSID.fast().toLong(), requestedAt, method, urlPattern, statusCode, durationMs, topic?.first, topic?.second, fcmTokenId, requestedAt, requestedAt,
        )
    }

    private fun insertApiLogs(requestedAts: List<LocalDateTime>) {
        jdbcTemplate.batchUpdate(
            """
            INSERT INTO api_log (id, requested_at, method, url_pattern, status_code, duration_ms, created_at, updated_at)
            VALUES (?, ?, 'GET', ?, 200, 10, ?, ?)
            """,
            requestedAts.map {
                arrayOf<Any>(TSID.fast().toLong(), it, TOPICS, it, it)
            },
        )
    }

    private fun insertFcmToken(id: Long, deviceType: DeviceType, active: Boolean, createdAt: LocalDateTime) {
        jdbcTemplate.update(
            "INSERT INTO fcm_token (id, token, device_type, is_active, language, created_at, updated_at) VALUES (?, ?, ?, ?, 'ko', ?, ?)",
            id, "token-$id", deviceType.name, active, createdAt, createdAt,
        )
    }

    private fun insertNotice(nttId: Long, summaryStatus: SummaryStatus, createdAt: LocalDateTime) {
        jdbcTemplate.update(
            """
            INSERT INTO notice (id, ntt_id, topic_code, topic_name, title, department, content_url, registration_date, is_attachment,
                notification_status, summary_status, created_at, updated_at)
            VALUES (?, ?, 1, 'GENERAL_NEWS', ?, '학사팀', ?, ?, FALSE, 'SENT', ?, ?, ?)
            """,
            TSID.fast().toLong(), nttId, "공지 $nttId", "https://www.ut.ac.kr/notice?nttId=$nttId", createdAt.toLocalDate(),
            summaryStatus.name, createdAt, createdAt,
        )
    }

    private fun apiLogStatistics(): List<Pair<LocalDate, ApiLogStatistics>> =
        jdbcTemplate.query("SELECT * FROM daily_api_log_statistics") { rs, _ ->
            rs.statisticsDate() to ApiLogStatistics(
                method = rs.getString("method"),
                urlPattern = rs.getString("url_pattern"),
                requestCount = rs.getLong("request_count"),
                clientErrorCount = rs.getLong("client_error_count"),
                serverErrorCount = rs.getLong("server_error_count"),
                totalDurationMs = rs.getLong("total_duration_ms"),
                maxDurationMs = rs.getInt("max_duration_ms"),
                deviceCount = rs.getInt("device_count"),
            )
        }

    private fun topicStatistics(): List<Pair<LocalDate, TopicStatistics>> =
        jdbcTemplate.query("SELECT * FROM daily_topic_statistics") { rs, _ ->
            rs.statisticsDate() to TopicStatistics(rs.getInt("topic_code"), rs.getString("topic_name"), rs.getLong("request_count"))
        }

    private fun dailyStatistics(): Map<LocalDate, DailyRow> =
        jdbcTemplate.query("SELECT * FROM daily_statistics") { rs, _ ->
            val snapshot = rs.getObject("active_ios_token_count")?.let {
                TokenSnapshot(
                    rs.getInt("active_ios_token_count"),
                    rs.getInt("active_aos_token_count"),
                    rs.getInt("inactive_ios_token_count"),
                    rs.getInt("inactive_aos_token_count"),
                )
            }
            rs.statisticsDate() to DailyRow(
                DailyTotals(
                    requestCount = rs.getLong("request_count"),
                    deviceCount = rs.getInt("device_count"),
                    iosDeviceCount = rs.getInt("ios_device_count"),
                    aosDeviceCount = rs.getInt("aos_device_count"),
                    noticeCount = rs.getInt("notice_count"),
                    summaryCompletedCount = rs.getInt("summary_completed_count"),
                    summaryFailedCount = rs.getInt("summary_failed_count"),
                    newTokenCount = rs.getInt("new_token_count"),
                ),
                snapshot,
            )
        }.toMap()

    private fun snapshotAll() =
        Triple(apiLogStatistics().toSet(), topicStatistics().toSet(), dailyStatistics())

    /** 다시 집계해도 행을 새로 만들지 않고 기존 행(id · 생성 시각)을 고쳐 쓰는지 본다. */
    private fun rowKeys(): Map<String, Set<Pair<Long, LocalDateTime>>> =
        STATISTICS_TABLES.associateWith { table ->
            jdbcTemplate.query("SELECT id, created_at FROM $table") { rs, _ ->
                rs.getLong("id") to rs.getObject("created_at", LocalDateTime::class.java)
            }.toSet()
        }

    private fun count(table: String): Int =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM $table", Int::class.java)!!

    private fun step(execution: JobExecution, stepName: String) =
        execution.stepExecutions.single {
            it.stepName == stepName
        }

    private fun ResultSet.statisticsDate(): LocalDate =
        getObject("statistics_date", LocalDate::class.java)

    private data class DailyRow(val totals: DailyTotals, val snapshot: TokenSnapshot?)

    companion object {
        private val PARAMETERS = mapOf("lookbackDays" to "3", "apiLogRetentionDays" to "30")
        private val STATISTICS_TABLES = listOf("daily_api_log_statistics", "daily_topic_statistics", "daily_statistics")
        private val LAST_MOMENT = LocalTime.of(23, 59, 59, 999_999_000)

        private const val TOPICS = "/open-api/v1/topics"
        private const val NOTICES_V1 = "/open-api/v1/notices"
        private const val NOTICES_V2 = "/open-api/v2/notices"
        private const val LANGUAGE = "/open-api/v1/fcm-tokens/language"
        private val GENERAL_NEWS = 1 to "GENERAL_NEWS"
        private val COMPUTER_SOFTWARE = 300 to "COMPUTER_SOFTWARE"

        private const val IOS_ACTIVE = 101L
        private const val AOS_ACTIVE = 102L
        private const val AOS_INACTIVE = 103L
        private const val IOS_INACTIVE = 104L
        private const val UNKNOWN_ACTIVE = 105L
    }

}
