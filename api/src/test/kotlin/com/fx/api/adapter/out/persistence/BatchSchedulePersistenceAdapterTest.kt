package com.fx.api.adapter.out.persistence

import com.fx.api.exception.BatchException
import com.fx.api.exception.errorcode.BatchErrorCode
import com.fx.common.domain.batch.BatchSchedule
import com.fx.persistence.MySqlContainerConfig
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import

/** 실제 MySQL 에서 Flyway 로 시드된 스케줄을 조회 · 저장한다. */
@DataJpaTest(properties = ["MYSQL_URL=unused", "MYSQL_DATABASE=unused", "MYSQL_USERNAME=unused", "MYSQL_PASSWORD=unused"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MySqlContainerConfig::class, BatchSchedulePersistenceAdapter::class)
class BatchSchedulePersistenceAdapterTest @Autowired constructor(
    private val batchSchedulePersistenceAdapter: BatchSchedulePersistenceAdapter,
) {

    @Test
    fun `스케줄을 scheduleKey 순으로 조회한다`() {
        val scheduleKeys = batchSchedulePersistenceAdapter.findAll().map {
            it.scheduleKey
        }

        assertThat(scheduleKeys).containsExactly(
            "batch-maintenance",
            "meal-notify",
            "notice-crawl-major",
            "notice-crawl-notice",
            "notice-summary",
            "seat-alert-check",
            "silent-push",
        )
    }

    @Test
    fun `스케줄 키로 조회하고, 없으면 SCHEDULE_NOT_FOUND 예외가 발생한다`() {
        assertThat(batchSchedulePersistenceAdapter.getByScheduleKey("silent-push").jobName).isEqualTo("silentPushJob")
        assertThat(batchSchedulePersistenceAdapter.existsByScheduleKey("silent-push")).isTrue()
        assertThat(batchSchedulePersistenceAdapter.existsByScheduleKey("unknown")).isFalse()

        assertThatThrownBy {
            batchSchedulePersistenceAdapter.getByScheduleKey("unknown")
        }.isInstanceOfSatisfying(BatchException::class.java) {
            assertThat(it.baseErrorCode).isEqualTo(BatchErrorCode.SCHEDULE_NOT_FOUND)
        }
    }

    @Test
    fun `새 스케줄을 저장하고, 같은 키가 이미 있으면 SCHEDULE_KEY_DUPLICATED 예외가 발생한다`() {
        val saved = batchSchedulePersistenceAdapter.create(schedule("notice-crawl-notice-night"))
        assertThat(saved.id).isNotNull()
        assertThat(batchSchedulePersistenceAdapter.existsByScheduleKey("notice-crawl-notice-night")).isTrue()

        assertThatThrownBy {
            batchSchedulePersistenceAdapter.create(schedule("silent-push"))
        }.isInstanceOfSatisfying(BatchException::class.java) {
            assertThat(it.baseErrorCode).isEqualTo(BatchErrorCode.SCHEDULE_KEY_DUPLICATED)
        }
    }

    private fun schedule(scheduleKey: String) =
        BatchSchedule(scheduleKey, "noticeCrawlJob", """{"topicType":"NOTICE"}""", "0 0 23 * * *", false, "공지 게시판 야간 크롤링")

}
