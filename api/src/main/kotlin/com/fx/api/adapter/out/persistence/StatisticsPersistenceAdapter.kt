package com.fx.api.adapter.out.persistence

import com.fx.api.application.port.out.statistics.StatisticsPersistencePort
import com.fx.api.domain.DailyEndpointStatistics
import com.fx.api.domain.DailyStatistics
import com.fx.api.domain.DailyTokenStatistics
import com.fx.api.domain.DailyTopicStatistics
import com.fx.api.domain.DeviceCounts
import com.fx.api.domain.EndpointStatistics
import com.fx.api.domain.RequestCounts
import com.fx.api.domain.StatisticsPeriod
import com.fx.api.domain.TopicRequestCount
import com.fx.common.annotation.PersistenceAdapter
import com.fx.common.domain.DeviceType
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 일일 통계 테이블은 crawler 가 엔티티 없이 JDBC 로 쓰므로 api 도 JDBC 로 읽는다.
 *
 * `daily_statistics` 에 행이 있는 날을 집계된 날로 본다. 일일 통계 Job 은 집계한 날짜마다 이 테이블에 항상 한 행을 쓰고,
 * 엔드포인트 · 토픽 통계에는 요청이 있었던 것만 쓴다. 그래서 하루치 추이는 `daily_statistics` 에 LEFT JOIN 해서
 * 집계된 날의 빈 값은 0 으로 채우고 집계되지 않은 날은 뺀다.
 */
@PersistenceAdapter
class StatisticsPersistenceAdapter(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) : StatisticsPersistencePort {

    override fun findDailyStatistics(period: StatisticsPeriod): List<DailyStatistics> =
        jdbcTemplate.query(
            """
            SELECT statistics_date, request_count, device_count, ios_device_count, aos_device_count,
                   notice_count, summary_completed_count, summary_failed_count, new_token_count, updated_at
            FROM daily_statistics
            WHERE statistics_date BETWEEN :from AND :to
            ORDER BY statistics_date
            """,
            periodParams(period),
        ) { rs, _ ->
            DailyStatistics(
                statisticsDate = rs.localDate("statistics_date"),
                requestCount = rs.getLong("request_count"),
                deviceCount = rs.getInt("device_count"),
                iosDeviceCount = rs.getInt("ios_device_count"),
                aosDeviceCount = rs.getInt("aos_device_count"),
                noticeCount = rs.getInt("notice_count"),
                summaryCompletedCount = rs.getInt("summary_completed_count"),
                summaryFailedCount = rs.getInt("summary_failed_count"),
                newTokenCount = rs.getInt("new_token_count"),
                aggregatedAt = requireNotNull(rs.getObject("updated_at", LocalDateTime::class.java)),
            )
        }

    /** 스냅샷은 네 컬럼을 함께 쓰므로 활성 · 비활성 중 하나만 비어 있는 경우는 없다. */
    override fun findDailyTokenStatistics(period: StatisticsPeriod): List<DailyTokenStatistics> =
        jdbcTemplate.query(
            """
            SELECT statistics_date, new_token_count,
                   active_ios_token_count, active_aos_token_count,
                   inactive_ios_token_count, inactive_aos_token_count
            FROM daily_statistics
            WHERE statistics_date BETWEEN :from AND :to
            ORDER BY statistics_date
            """,
            periodParams(period),
        ) { rs, _ ->
            DailyTokenStatistics(
                statisticsDate = rs.localDate("statistics_date"),
                newTokenCount = rs.getInt("new_token_count"),
                active = rs.deviceCountsOrNull("active_ios_token_count", "active_aos_token_count"),
                inactive = rs.deviceCountsOrNull("inactive_ios_token_count", "inactive_aos_token_count"),
            )
        }

    override fun findEndpointStatistics(period: StatisticsPeriod): List<EndpointStatistics> =
        jdbcTemplate.query(
            """
            SELECT method,
                   url_pattern,
                   SUM(request_count)      AS request_count,
                   SUM(client_error_count) AS client_error_count,
                   SUM(server_error_count) AS server_error_count,
                   SUM(total_duration_ms)  AS total_duration_ms,
                   MAX(max_duration_ms)    AS max_duration_ms
            FROM daily_api_log_statistics
            WHERE statistics_date BETWEEN :from AND :to
            GROUP BY method, url_pattern
            ORDER BY request_count DESC, url_pattern, method
            """,
            periodParams(period),
        ) { rs, _ ->
            EndpointStatistics(
                method = rs.getString("method"),
                urlPattern = rs.getString("url_pattern"),
                counts = rs.requestCounts(),
            )
        }

    override fun findDailyEndpointStatistics(
        method: String,
        urlPattern: String,
        period: StatisticsPeriod,
    ): List<DailyEndpointStatistics> =
        jdbcTemplate.query(
            """
            SELECT d.statistics_date,
                   COALESCE(a.request_count, 0)      AS request_count,
                   COALESCE(a.client_error_count, 0) AS client_error_count,
                   COALESCE(a.server_error_count, 0) AS server_error_count,
                   COALESCE(a.total_duration_ms, 0)  AS total_duration_ms,
                   a.max_duration_ms,
                   COALESCE(a.device_count, 0)       AS device_count
            FROM daily_statistics d
                     LEFT JOIN daily_api_log_statistics a
                               ON a.statistics_date = d.statistics_date
                                   AND a.method = :method
                                   AND a.url_pattern = :urlPattern
            WHERE d.statistics_date BETWEEN :from AND :to
            ORDER BY d.statistics_date
            """,
            periodParams(period)
                .addValue("method", method)
                .addValue("urlPattern", urlPattern),
        ) { rs, _ ->
            DailyEndpointStatistics(
                statisticsDate = rs.localDate("statistics_date"),
                counts = rs.requestCounts(),
                deviceCount = rs.getInt("device_count"),
            )
        }

    override fun findTopicRequestCounts(period: StatisticsPeriod): List<TopicRequestCount> =
        jdbcTemplate.query(
            """
            SELECT topic_code, topic_name, SUM(request_count) AS request_count
            FROM daily_topic_statistics
            WHERE statistics_date BETWEEN :from AND :to
            GROUP BY topic_code, topic_name
            """,
            periodParams(period),
        ) { rs, _ ->
            TopicRequestCount(
                topicCode = rs.getInt("topic_code"),
                topicName = rs.getString("topic_name"),
                requestCount = rs.getLong("request_count"),
            )
        }

    override fun findDailyTopicStatistics(topicCode: Int, period: StatisticsPeriod): List<DailyTopicStatistics> =
        jdbcTemplate.query(
            """
            SELECT d.statistics_date, COALESCE(t.request_count, 0) AS request_count
            FROM daily_statistics d
                     LEFT JOIN daily_topic_statistics t
                               ON t.statistics_date = d.statistics_date
                                   AND t.topic_code = :topicCode
            WHERE d.statistics_date BETWEEN :from AND :to
            ORDER BY d.statistics_date
            """,
            periodParams(period)
                .addValue("topicCode", topicCode),
        ) { rs, _ ->
            DailyTopicStatistics(
                statisticsDate = rs.localDate("statistics_date"),
                requestCount = rs.getLong("request_count"),
            )
        }

    override fun countSubscribersByTopic(): Map<Int, DeviceCounts> =
        jdbcTemplate.query(
            """
            SELECT s.topic_code,
                   COUNT(*)                                             AS total_count,
                   SUM(CASE WHEN f.device_type = :ios THEN 1 ELSE 0 END) AS ios_count,
                   SUM(CASE WHEN f.device_type = :aos THEN 1 ELSE 0 END) AS aos_count
            FROM fcm_token_subscription s
                     JOIN fcm_token f ON f.id = s.fcm_token_id
            WHERE f.is_active = TRUE
            GROUP BY s.topic_code
            """,
            deviceTypeParams(),
        ) { rs, _ ->
            rs.getInt("topic_code") to rs.deviceCounts()
        }.toMap()

    override fun countActiveTokens(): DeviceCounts =
        requireNotNull(
            jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)                                                         AS total_count,
                       COALESCE(SUM(CASE WHEN device_type = :ios THEN 1 ELSE 0 END), 0) AS ios_count,
                       COALESCE(SUM(CASE WHEN device_type = :aos THEN 1 ELSE 0 END), 0) AS aos_count
                FROM fcm_token
                WHERE is_active = TRUE
                """,
                deviceTypeParams(),
            ) { rs, _ ->
                rs.deviceCounts()
            }
        )

    private fun periodParams(period: StatisticsPeriod): MapSqlParameterSource =
        MapSqlParameterSource()
            .addValue("from", period.from)
            .addValue("to", period.to)

    private fun deviceTypeParams(): MapSqlParameterSource =
        MapSqlParameterSource()
            .addValue("ios", DeviceType.iOS.name)
            .addValue("aos", DeviceType.AOS.name)

    private fun ResultSet.localDate(column: String): LocalDate =
        requireNotNull(getObject(column, LocalDate::class.java))

    private fun ResultSet.requestCounts(): RequestCounts =
        RequestCounts(
            requestCount = getLong("request_count"),
            clientErrorCount = getLong("client_error_count"),
            serverErrorCount = getLong("server_error_count"),
            totalDurationMs = getLong("total_duration_ms"),
            maxDurationMs = (getObject("max_duration_ms") as Number?)?.toInt(),
        )

    private fun ResultSet.deviceCounts(): DeviceCounts =
        DeviceCounts(
            total = getLong("total_count"),
            ios = getLong("ios_count"),
            aos = getLong("aos_count"),
        )

    /** 스냅샷 컬럼이 비어 있으면(그날 기록하지 못했으면) null */
    private fun ResultSet.deviceCountsOrNull(iosColumn: String, aosColumn: String): DeviceCounts? {
        val ios =
            (getObject(iosColumn) as Number?)?.toLong()
        val aos =
            (getObject(aosColumn) as Number?)?.toLong()
        return if (ios == null || aos == null) null else DeviceCounts.of(ios, aos)
    }

}
