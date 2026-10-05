package com.fx.migration

import com.fx.persistence.MySqlContainerConfig
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.Assertions.entry
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

/**
 * 레거시 앱이 저장하던 형태 그대로 MongoDB 에 넣고, 이관 결과를 MySQL 에서 확인한다.
 * chunk 크기를 2 로 줄여 여러 chunk 에 걸쳐 옮기는 경우도 함께 검증한다.
 */
@SpringBootTest(
    properties = [
        "migration.run-on-startup=false",
        "migration.chunk-size=2",
        "MONGO_URI=mongodb://unused",
        "MONGO_DATABASE=knutice",
        "MYSQL_URL=unused",
        "MYSQL_DATABASE=unused",
        "MYSQL_USERNAME=unused",
        "MYSQL_PASSWORD=unused",
    ]
)
@Import(MySqlContainerConfig::class, MongoContainerConfig::class)
class MigrationServiceTest @Autowired constructor(
    private val migrationService: MigrationService,
    private val mongoTemplate: MongoTemplate,
    private val jdbcTemplate: JdbcTemplate,
) {

    private val seoul = ZoneId.of("Asia/Seoul")

    @BeforeEach
    fun reset() {
        mongoTemplate.db.drop()
        listOf("report", "image", "tip", "users", "fcm_token_subscription", "fcm_token", "notice_content", "notice")
            .forEach {
                jdbcTemplate.update("DELETE FROM $it")
            }
    }

    @Test
    fun `레거시 컬렉션을 새 스키마로 옮기고 건수를 검증한다`() {
        insertLegacyData()

        val report = migrationService.migrate()

        assertThat(report.successful).isTrue()
        val results = report.collections.associateBy {
            it.name
        }
        assertThat(results.getValue("notice").skipped).containsExactly(entry("모르는 토픽: REMOVED_TOPIC", 1))
        assertThat(results.getValue("notice").notes).containsEntry("제목 500자 초과로 자름", 1)
        assertThat(results.getValue("fcm_token").notes).containsEntry("모르는 구독 토픽: UNKNOWN_TOPIC", 1)
        assertThat(results.getValue("user").skipped).containsEntry("이메일 중복", 1)
        assertThat(results.getValue("report").notes).containsEntry("토큰이 없어 비활성 토큰을 만들어 연결", 1)
        assertThat(report.excluded).containsEntry("api_log", 1L).containsEntry("seat_alert", 0L)

        assertNotices()
        assertFcmTokens()
        assertApiData()
    }

    @Test
    fun `대상 테이블에 데이터가 있으면 이관하지 않는다`() {
        insertLegacyData()
        migrationService.migrate()

        assertThatThrownBy {
            migrationService.migrate()
        }.isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("notice")
    }

    private fun assertNotices() {
        val notice = jdbcTemplate.queryForMap("SELECT * FROM notice WHERE ntt_id = 1001")
        assertThat(notice["topic_code"]).isEqualTo(1)
        assertThat(notice["topic_name"]).isEqualTo("GENERAL_NEWS")
        // 레거시가 전날 15:00Z 로 저장한 날짜가 하루 밀리지 않는다
        assertThat(notice["registration_date"]).isEqualTo(java.sql.Date.valueOf(LocalDate.of(2026, 9, 29)))
        assertThat(notice["is_attachment"]).isEqualTo(true)
        assertThat(notice["notification_status"]).isEqualTo("SENT")
        assertThat(notice["summary_status"]).isEqualTo("COMPLETED")
        assertThat(dateTime("SELECT created_at FROM notice WHERE ntt_id = 1001")).isEqualTo(LocalDateTime.of(2026, 9, 29, 10, 0))
        assertThat(dateTime("SELECT notified_at FROM notice WHERE ntt_id = 1001")).isEqualTo(LocalDateTime.of(2026, 9, 29, 10, 0))
        assertThat(dateTime("SELECT updated_at FROM notice WHERE ntt_id = 1001")).isEqualTo(LocalDateTime.of(2026, 9, 29, 11, 30))

        val content = jdbcTemplate.queryForMap(
            "SELECT c.content, c.content_summary FROM notice_content c JOIN notice n ON n.id = c.notice_id WHERE n.ntt_id = 1001"
        )
        assertThat(content).containsEntry("content", "본문").containsEntry("content_summary", "요약")

        assertThat(jdbcTemplate.queryForMap("SELECT topic_code, summary_status FROM notice WHERE ntt_id = 1002"))
            .containsEntry("topic_code", 300).containsEntry("summary_status", "SKIPPED")
        assertThat(jdbcTemplate.queryForObject("SELECT CHAR_LENGTH(title) FROM notice WHERE ntt_id = 1004", Int::class.java)).isEqualTo(500)
        assertThat(count("SELECT COUNT(*) FROM notice WHERE ntt_id = 1003")).isZero()
    }

    private fun assertFcmTokens() {
        val tokens = jdbcTemplate.queryForList("SELECT token, device_type, is_active, language FROM fcm_token ORDER BY token")
        assertThat(tokens.map {
            listOf(it["token"], it["device_type"], it["is_active"], it["language"])
        }).containsExactly(
            listOf("deleted-token", "UNKNOWN", false, "ko"),
            listOf("token-1", "iOS", true, "ko"),
            listOf("token-2", "AOS", false, "ko"),
        )
        assertThat(subscribedCodes("token-1")).containsExactly(1, 2, 300, 900)
        assertThat(subscribedCodes("token-2")).containsExactly(1)
        assertThat(
            jdbcTemplate.queryForList("SELECT DISTINCT topic_name FROM fcm_token_subscription WHERE topic_code = 300", String::class.java)
        ).containsExactly("COMPUTER_ENGINEERING")
    }

    private fun assertApiData() {
        assertThat(jdbcTemplate.queryForList("SELECT email FROM users", String::class.java)).containsExactly("admin@ut.ac.kr")
        // 레거시 생성 순서대로 id 가 커지므로 최신순 목록 순서가 그대로다
        assertThat(jdbcTemplate.queryForList("SELECT title FROM tip ORDER BY id", String::class.java)).containsExactly("첫 팁", "둘째 팁", "셋째 팁")
        assertThat(jdbcTemplate.queryForList("SELECT image_type FROM image ORDER BY id", String::class.java)).containsExactly("DEFAULT_IMAGE", "TIP_IMAGE")

        val reports = jdbcTemplate.queryForList(
            "SELECT r.content, t.token FROM report r JOIN fcm_token t ON t.id = r.fcm_token_id ORDER BY r.id"
        )
        assertThat(reports.map {
            it["content"] to it["token"]
        }).containsExactly(
            "알림이 안 와요" to "token-1",
            "앱을 지운 뒤 남긴 문의" to "deleted-token",
        )
    }

    private fun insertLegacyData() {
        val created = legacyDateTime(LocalDateTime.of(2026, 9, 29, 10, 0))
        insert("notice",
            Document("_id", 1001L).append("title", "수강신청 안내").append("department", "학사팀")
                .append("contentUrl", "https://www.ut.ac.kr/notice?nttId=1001").append("content", "본문").append("contentSummary", "요약")
                .append("registrationDate", legacyDate(LocalDate.of(2026, 9, 29))).append("isAttachment", true).append("topic", "GENERAL_NEWS")
                .append("createdAt", created).append("updatedAt", legacyDateTime(LocalDateTime.of(2026, 9, 29, 11, 30))),
            Document("_id", 1002L).append("title", "학과 공지").append("department", "컴퓨터공학과")
                .append("contentUrl", "https://ce.ut.ac.kr/notice?nttId=1002").append("contentSummary", "")
                .append("registrationDate", legacyDate(LocalDate.of(2026, 9, 28))).append("isAttachment", false).append("topic", "COMPUTER_ENGINEERING")
                .append("createdAt", created),
            Document("_id", 1003L).append("title", "없어진 게시판").append("department", "학사팀")
                .append("contentUrl", "https://www.ut.ac.kr/notice?nttId=1003")
                .append("registrationDate", legacyDate(LocalDate.of(2026, 9, 28))).append("topic", "REMOVED_TOPIC"),
            Document("_id", 1004L).append("title", "가".repeat(600)).append("department", "학사팀")
                .append("contentUrl", "https://www.ut.ac.kr/notice?nttId=1004")
                .append("registrationDate", legacyDate(LocalDate.of(2026, 9, 28))).append("topic", "GENERAL_NEWS")
                .append("createdAt", created),
        )
        insert("fcm_token",
            Document("_id", "token-1")
                .append("subscribedNoticeTopics", listOf("GENERAL_NEWS", "SCHOLARSHIP_NEWS"))
                .append("subscribedMajorTopics", listOf("COMPUTER_ENGINEERING"))
                .append("subscribedMealTopics", listOf("STUDENT_CAFETERIA"))
                .append("deviceType", "iOS").append("isActive", true).append("createdAt", created),
            Document("_id", "token-2")
                .append("subscribedNoticeTopics", listOf("GENERAL_NEWS", "UNKNOWN_TOPIC"))
                .append("deviceType", "AOS").append("isActive", false).append("createdAt", created),
        )
        insert("user",
            Document("_id", ObjectId()).append("email", "admin@ut.ac.kr").append("password", "\$2a\$10\$hash").append("nickname", "관리자").append("role", "ADMIN"),
            Document("_id", ObjectId()).append("email", "ADMIN@ut.ac.kr").append("password", "\$2a\$10\$hash").append("nickname", "관리자2").append("role", "ADMIN"),
        )
        insert("tip", *listOf("첫 팁", "둘째 팁", "셋째 팁").map {
            Document("_id", ObjectId()).append("title", it).append("url", "https://blog/$it").append("deviceType", "iOS").append("createdAt", created)
        }.toTypedArray())
        insert("image",
            Document("_id", ObjectId()).append("imageUrl", "https://img/a.png").append("originalName", "a.png").append("serverName", "uuid-a").append("extension", "png").append("type", "DEFAULT_IMAGE"),
            Document("_id", ObjectId()).append("imageUrl", "https://img/b.png").append("originalName", "b.png").append("serverName", "uuid-b").append("extension", "png").append("type", "TIP_IMAGE"),
        )
        insert("report",
            Document("_id", ObjectId()).append("fcmToken", "token-1").append("content", "알림이 안 와요").append("deviceName", "iPhone").append("version", "1.7.3").append("createdAt", created),
            Document("_id", ObjectId()).append("fcmToken", "deleted-token").append("content", "앱을 지운 뒤 남긴 문의").append("deviceName", "Galaxy").append("version", "1.7.0").append("createdAt", created),
        )
        insert("api_log", Document("_id", ObjectId()).append("uri", "/open-api/v1/notices"))
    }

    private fun insert(collection: String, vararg documents: Document) {
        mongoTemplate.getCollection(collection).insertMany(documents.toList())
    }

    /** 레거시 앱(Spring Data MongoDB)이 `LocalDateTime` 을 저장하던 방식 : 시스템 타임존(KST) 기준 시각의 UTC Date */
    private fun legacyDateTime(value: LocalDateTime): Date =
        Date.from(value.atZone(seoul).toInstant())

    /** 레거시 앱이 `LocalDate` 를 저장하던 방식 : KST 자정 = 전날 15:00Z */
    private fun legacyDate(value: LocalDate): Date =
        Date.from(value.atStartOfDay(seoul).toInstant())

    private fun dateTime(sql: String): LocalDateTime? =
        jdbcTemplate.queryForObject(sql, LocalDateTime::class.java)

    private fun count(sql: String): Int =
        jdbcTemplate.queryForObject(sql, Int::class.java)!!

    private fun subscribedCodes(token: String): List<Int> =
        jdbcTemplate.queryForList(
            "SELECT s.topic_code FROM fcm_token_subscription s JOIN fcm_token t ON t.id = s.fcm_token_id WHERE t.token = ? ORDER BY s.topic_code",
            Int::class.java,
            token,
        ).filterNotNull()

}
