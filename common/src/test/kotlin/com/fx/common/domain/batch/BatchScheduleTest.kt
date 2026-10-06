package com.fx.common.domain.batch

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class BatchScheduleTest {

    private val now = LocalDateTime.of(2026, 9, 30, 10, 7)

    private fun schedule(enabled: Boolean = true) =
        BatchSchedule(
            scheduleKey = "notice-crawl-notice",
            jobName = BatchJob.NOTICE_CRAWL.jobName,
            jobParameters = """{"topicType":"NOTICE"}""",
            cron = "0 0/15 * * * *",
            enabled = enabled,
            description = "공지 크롤링",
        )

    @Test
    fun `잘못된 cron 으로는 만들 수 없다`() {
        assertThatThrownBy {
            BatchSchedule("key", BatchJob.SILENT_PUSH.jobName, "{}", "*/10 * * * * *", true, "설명")
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `cron 을 바꾸면 다음 발화 시각을 다시 계산한다`() {
        val schedule = schedule()

        schedule.changeCron("0 0 * * * *", now)

        assertThat(schedule.cron).isEqualTo("0 0 * * * *")
        assertThat(schedule.nextFireAt).isEqualTo(LocalDateTime.of(2026, 9, 30, 11, 0))
    }

    @Test
    fun `꺼진 스케줄은 cron 을 바꿔도 발화 시각이 없다`() {
        val schedule = schedule(enabled = false)

        schedule.changeCron("0 0 * * * *", now)

        assertThat(schedule.nextFireAt).isNull()
    }

    @Test
    fun `켜면 지금 이후의 발화 시각부터 세고, 끄면 발화 시각을 지운다`() {
        val schedule = schedule(enabled = false)

        schedule.enable(now)
        assertThat(schedule.enabled).isTrue()
        assertThat(schedule.nextFireAt).isEqualTo(LocalDateTime.of(2026, 9, 30, 10, 15))

        schedule.disable()
        assertThat(schedule.enabled).isFalse()
        assertThat(schedule.nextFireAt).isNull()
    }

}
