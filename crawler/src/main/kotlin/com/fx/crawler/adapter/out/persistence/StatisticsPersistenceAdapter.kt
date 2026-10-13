package com.fx.crawler.adapter.out.persistence

import com.fx.common.annotation.PersistenceAdapter
import com.fx.common.domain.DeviceType
import com.fx.common.domain.notice.SummaryStatus
import com.fx.crawler.application.port.out.StatisticsPersistencePort
import com.fx.crawler.domain.statistics.ApiLogStatistics
import com.fx.crawler.domain.statistics.DailyTotals
import com.fx.crawler.domain.statistics.TokenSnapshot
import com.fx.crawler.domain.statistics.TopicStatistics
import io.hypersistence.tsid.TSID
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 통계 테이블은 SQL 로만 다루므로 엔티티 없이 JDBC 로 집계 · 저장한다.
 *
 * 집계는 DB 에서 한 번에 하고, 저장은 앱에서 새 행의 TSID 를 붙여 upsert 한다.
 * `INSERT … SELECT … ON DUPLICATE KEY UPDATE` 한 문장으로는 새 행의 TSID 를 만들 수 없기 때문이다.
 * 기존 행은 `id` · `created_at` 을 그대로 두고 값만 새 집계 결과로 바꾼다.
 */
@PersistenceAdapter
class StatisticsPersistenceAdapter(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) : StatisticsPersistencePort {

    override fun aggregateApiLogs(from: LocalDateTime, to: LocalDateTime): List<ApiLogStatistics> =
        jdbcTemplate.query(
            """
            SELECT method,
                   url_pattern,
                   COUNT(*)                                                         AS request_count,
                   SUM(CASE WHEN status_code BETWEEN 400 AND 499 THEN 1 ELSE 0 END) AS client_error_count,
                   SUM(CASE WHEN status_code >= 500 THEN 1 ELSE 0 END)              AS server_error_count,
                   SUM(duration_ms)                                                 AS total_duration_ms,
                   MAX(duration_ms)                                                 AS max_duration_ms,
                   COUNT(DISTINCT fcm_token_id)                                     AS device_count
            FROM api_log
            WHERE requested_at >= :from
              AND requested_at < :to
            GROUP BY method, url_pattern
            """,
            range(from, to),
        ) { rs, _ ->
            ApiLogStatistics(
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

    override fun aggregateTopics(from: LocalDateTime, to: LocalDateTime): List<TopicStatistics> =
        jdbcTemplate.query(
            """
            SELECT topic_code,
                   topic_name,
                   COUNT(*) AS request_count
            FROM api_log
            WHERE requested_at >= :from
              AND requested_at < :to
              AND topic_code IS NOT NULL
            GROUP BY topic_code, topic_name
            """,
            range(from, to),
        ) { rs, _ ->
            TopicStatistics(
                topicCode = rs.getInt("topic_code"),
                topicName = rs.getString("topic_name"),
                requestCount = rs.getLong("request_count"),
            )
        }

    /** API 로그 · 공지 · 토큰을 각각 집계한 1행짜리 결과를 묶어 한 문장으로 읽는다. */
    override fun aggregateTotals(from: LocalDateTime, to: LocalDateTime): DailyTotals =
        jdbcTemplate.queryForObject(
            """
            SELECT a.request_count, a.device_count, a.ios_device_count, a.aos_device_count,
                   n.notice_count, n.summary_completed_count, n.summary_failed_count,
                   t.new_token_count
            FROM (SELECT COUNT(*)                                                               AS request_count,
                         COUNT(DISTINCT l.fcm_token_id)                                         AS device_count,
                         COUNT(DISTINCT CASE WHEN f.device_type = :ios THEN l.fcm_token_id END) AS ios_device_count,
                         COUNT(DISTINCT CASE WHEN f.device_type = :aos THEN l.fcm_token_id END) AS aos_device_count
                  FROM api_log l
                           LEFT JOIN fcm_token f ON f.id = l.fcm_token_id
                  WHERE l.requested_at >= :from
                    AND l.requested_at < :to) a,
                 (SELECT COUNT(*)                                                                   AS notice_count,
                         COALESCE(SUM(CASE WHEN summary_status = :completed THEN 1 ELSE 0 END), 0) AS summary_completed_count,
                         COALESCE(SUM(CASE WHEN summary_status = :failed THEN 1 ELSE 0 END), 0)    AS summary_failed_count
                  FROM notice
                  WHERE created_at >= :from
                    AND created_at < :to) n,
                 (SELECT COUNT(*) AS new_token_count
                  FROM fcm_token
                  WHERE created_at >= :from
                    AND created_at < :to) t
            """,
            range(from, to)
                .addValue("ios", DeviceType.iOS.name)
                .addValue("aos", DeviceType.AOS.name)
                .addValue("completed", SummaryStatus.COMPLETED.name)
                .addValue("failed", SummaryStatus.FAILED.name),
        ) { rs, _ ->
            DailyTotals(
                requestCount = rs.getLong("request_count"),
                deviceCount = rs.getInt("device_count"),
                iosDeviceCount = rs.getInt("ios_device_count"),
                aosDeviceCount = rs.getInt("aos_device_count"),
                noticeCount = rs.getInt("notice_count"),
                summaryCompletedCount = rs.getInt("summary_completed_count"),
                summaryFailedCount = rs.getInt("summary_failed_count"),
                newTokenCount = rs.getInt("new_token_count"),
            )
        }

    override fun captureTokenSnapshot(): TokenSnapshot =
        jdbcTemplate.queryForObject(
            """
            SELECT COALESCE(SUM(CASE WHEN is_active = TRUE  AND device_type = :ios THEN 1 ELSE 0 END), 0) AS active_ios_token_count,
                   COALESCE(SUM(CASE WHEN is_active = TRUE  AND device_type = :aos THEN 1 ELSE 0 END), 0) AS active_aos_token_count,
                   COALESCE(SUM(CASE WHEN is_active = FALSE AND device_type = :ios THEN 1 ELSE 0 END), 0) AS inactive_ios_token_count,
                   COALESCE(SUM(CASE WHEN is_active = FALSE AND device_type = :aos THEN 1 ELSE 0 END), 0) AS inactive_aos_token_count
            FROM fcm_token
            """,
            MapSqlParameterSource()
                .addValue("ios", DeviceType.iOS.name)
                .addValue("aos", DeviceType.AOS.name),
        ) { rs, _ ->
            TokenSnapshot(
                activeIosTokenCount = rs.getInt("active_ios_token_count"),
                activeAosTokenCount = rs.getInt("active_aos_token_count"),
                inactiveIosTokenCount = rs.getInt("inactive_ios_token_count"),
                inactiveAosTokenCount = rs.getInt("inactive_aos_token_count"),
            )
        }

    override fun saveApiLogStatistics(date: LocalDate, statistics: List<ApiLogStatistics>, now: LocalDateTime) {
        if (statistics.isEmpty()) {
            return
        }
        jdbcTemplate.batchUpdate(
            """
            INSERT INTO daily_api_log_statistics
                (id, statistics_date, method, url_pattern, request_count, client_error_count, server_error_count,
                 total_duration_ms, max_duration_ms, device_count, created_at, updated_at)
            VALUES (:id, :statisticsDate, :method, :urlPattern, :requestCount, :clientErrorCount, :serverErrorCount,
                    :totalDurationMs, :maxDurationMs, :deviceCount, :now, :now) AS new
            ON DUPLICATE KEY UPDATE request_count      = new.request_count,
                                    client_error_count = new.client_error_count,
                                    server_error_count = new.server_error_count,
                                    total_duration_ms  = new.total_duration_ms,
                                    max_duration_ms    = new.max_duration_ms,
                                    device_count       = new.device_count,
                                    updated_at         = new.updated_at
            """,
            statistics.map {
                newRow(date, now)
                    .addValue("method", it.method)
                    .addValue("urlPattern", it.urlPattern)
                    .addValue("requestCount", it.requestCount)
                    .addValue("clientErrorCount", it.clientErrorCount)
                    .addValue("serverErrorCount", it.serverErrorCount)
                    .addValue("totalDurationMs", it.totalDurationMs)
                    .addValue("maxDurationMs", it.maxDurationMs)
                    .addValue("deviceCount", it.deviceCount)
            }.toTypedArray(),
        )
    }

    override fun saveTopicStatistics(date: LocalDate, statistics: List<TopicStatistics>, now: LocalDateTime) {
        if (statistics.isEmpty()) {
            return
        }
        jdbcTemplate.batchUpdate(
            """
            INSERT INTO daily_topic_statistics
                (id, statistics_date, topic_code, topic_name, request_count, created_at, updated_at)
            VALUES (:id, :statisticsDate, :topicCode, :topicName, :requestCount, :now, :now) AS new
            ON DUPLICATE KEY UPDATE topic_name    = new.topic_name,
                                    request_count = new.request_count,
                                    updated_at    = new.updated_at
            """,
            statistics.map {
                newRow(date, now)
                    .addValue("topicCode", it.topicCode)
                    .addValue("topicName", it.topicName)
                    .addValue("requestCount", it.requestCount)
            }.toTypedArray(),
        )
    }

    override fun saveTotals(date: LocalDate, totals: DailyTotals, now: LocalDateTime) {
        jdbcTemplate.update(
            """
            INSERT INTO daily_statistics
                (id, statistics_date, request_count, device_count, ios_device_count, aos_device_count,
                 notice_count, summary_completed_count, summary_failed_count, new_token_count, created_at, updated_at)
            VALUES (:id, :statisticsDate, :requestCount, :deviceCount, :iosDeviceCount, :aosDeviceCount,
                    :noticeCount, :summaryCompletedCount, :summaryFailedCount, :newTokenCount, :now, :now) AS new
            ON DUPLICATE KEY UPDATE request_count           = new.request_count,
                                    device_count            = new.device_count,
                                    ios_device_count        = new.ios_device_count,
                                    aos_device_count        = new.aos_device_count,
                                    notice_count            = new.notice_count,
                                    summary_completed_count = new.summary_completed_count,
                                    summary_failed_count    = new.summary_failed_count,
                                    new_token_count         = new.new_token_count,
                                    updated_at              = new.updated_at
            """,
            newRow(date, now)
                .addValue("requestCount", totals.requestCount)
                .addValue("deviceCount", totals.deviceCount)
                .addValue("iosDeviceCount", totals.iosDeviceCount)
                .addValue("aosDeviceCount", totals.aosDeviceCount)
                .addValue("noticeCount", totals.noticeCount)
                .addValue("summaryCompletedCount", totals.summaryCompletedCount)
                .addValue("summaryFailedCount", totals.summaryFailedCount)
                .addValue("newTokenCount", totals.newTokenCount),
        )
    }

    override fun saveTokenSnapshot(date: LocalDate, snapshot: TokenSnapshot, now: LocalDateTime) {
        jdbcTemplate.update(
            """
            UPDATE daily_statistics
            SET active_ios_token_count   = :activeIosTokenCount,
                active_aos_token_count   = :activeAosTokenCount,
                inactive_ios_token_count = :inactiveIosTokenCount,
                inactive_aos_token_count = :inactiveAosTokenCount,
                updated_at               = :now
            WHERE statistics_date = :statisticsDate
            """,
            MapSqlParameterSource()
                .addValue("statisticsDate", date)
                .addValue("activeIosTokenCount", snapshot.activeIosTokenCount)
                .addValue("activeAosTokenCount", snapshot.activeAosTokenCount)
                .addValue("inactiveIosTokenCount", snapshot.inactiveIosTokenCount)
                .addValue("inactiveAosTokenCount", snapshot.inactiveAosTokenCount)
                .addValue("now", now),
        )
    }

    override fun deleteApiLogsRequestedBefore(cutoff: LocalDateTime, limit: Int): Int =
        jdbcTemplate.update(
            "DELETE FROM api_log WHERE requested_at < :cutoff LIMIT :limit",
            mapOf("cutoff" to cutoff, "limit" to limit),
        )

    private fun range(from: LocalDateTime, to: LocalDateTime) =
        MapSqlParameterSource()
            .addValue("from", from)
            .addValue("to", to)

    private fun newRow(date: LocalDate, now: LocalDateTime) =
        MapSqlParameterSource()
            .addValue("id", TSID.fast().toLong())
            .addValue("statisticsDate", date)
            .addValue("now", now)

}
