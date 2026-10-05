package com.fx.crawler.support

import com.fx.persistence.MySqlContainerConfig
import com.google.firebase.messaging.FirebaseMessaging
import org.junit.jupiter.api.BeforeEach
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.job.parameters.JobParameters
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import org.springframework.batch.core.launch.JobOperator
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.test.JobOperatorTestUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.bean.override.mockito.MockitoBean

/**
 * crawler 전체 컨텍스트를 실제 MySQL(Testcontainers) 위에 띄워 Job 을 끝까지 실행한다.
 * 외부 시스템만 [FakeExternalAdapters] 로 바꾸고, Job 은 동기로 실행한다.
 * 모든 통합 테스트가 같은 설정을 쓰므로 컨텍스트와 컨테이너를 함께 쓴다. 업무 테이블은 테스트마다 비운다.
 */
@SpringBootTest(
    properties = [
        "spring.flyway.enabled=true",
        "crawler.poller.enabled=false",
        "crawler.launch.async=false",
        "crawler.push.chunk-size=2",
        "crawler.summary.chunk-size=2",
        "CRAWLER_PORT=0",
        "MYSQL_URL=unused",
        "MYSQL_DATABASE=unused",
        "MYSQL_USERNAME=unused",
        "MYSQL_PASSWORD=unused",
        "GEMINI_API_KEY=test",
        "FIREBASE_KEY_PATH=unused",
        "WEBHOOK_SLACK_URL=http://localhost",
        "READING_ROOM_ROOT_URL=http://localhost",
        "READING_ROOM_SEATS_ENDPOINT=/seats",
        "READING_ROOM_STATUS_ENDPOINT=/status",
    ]
)
@Import(MySqlContainerConfig::class, FakeExternalAdapters::class)
@MockitoBean(types = [FirebaseMessaging::class])
abstract class CrawlerIntegrationTest {

    @Autowired lateinit var jdbcTemplate: JdbcTemplate
    @Autowired lateinit var jobOperator: JobOperator
    @Autowired lateinit var jobRepository: JobRepository

    @Autowired lateinit var pushPort: FakePushPort
    @Autowired lateinit var noticeCrawlPort: FakeNoticeCrawlPort
    @Autowired lateinit var mealPort: FakeMealPort
    @Autowired lateinit var noticeSummaryPort: FakeNoticeSummaryPort
    @Autowired lateinit var readingRoomRemotePort: FakeReadingRoomRemotePort
    @Autowired lateinit var webhookPort: FakeWebhookPort

    @BeforeEach
    fun resetState() {
        listOf("notice", "notice_content", "fcm_token", "fcm_token_subscription", "seat_alert", "batch_run_request")
            .forEach {
                jdbcTemplate.update("DELETE FROM $it")
            }
        pushPort.reset()
        noticeCrawlPort.reset()
        mealPort.reset()
        noticeSummaryPort.reset()
        readingRoomRemotePort.reset()
        webhookPort.reset()
    }

    fun run(job: Job, parameters: Map<String, String> = emptyMap()): JobExecution {
        val utils = JobOperatorTestUtils(jobOperator, jobRepository).apply {
            this.job = job
        }
        val jobParameters: JobParameters = JobParametersBuilder(utils.uniqueJobParameters)
            .apply {
                parameters.forEach { (name, value) ->
                    addString(name, value)
                }
            }
            .toJobParameters()
        return utils.startJob(jobParameters)
    }

}
