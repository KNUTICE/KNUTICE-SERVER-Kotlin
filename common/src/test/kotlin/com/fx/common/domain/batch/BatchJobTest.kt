package com.fx.common.domain.batch

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class BatchJobTest {

    @Test
    fun `Job 이름으로 찾고, 모르는 이름이면 null 이다`() {
        assertThat(BatchJob.from("noticeCrawlJob")).isEqualTo(BatchJob.NOTICE_CRAWL)
        assertThat(BatchJob.from("unknownJob")).isNull()
    }

    @Test
    fun `Job 이름은 겹치지 않는다`() {
        val names = BatchJob.entries.map {
            it.jobName
        }

        assertThat(names).doesNotHaveDuplicates()
    }

    @Test
    fun `관리자 화면에 보여 줄 Job 과 파라미터 설명이 모두 있다`() {
        assertThat(BatchJob.entries).allSatisfy {
            assertThat(it.description).isNotBlank()
        }
        assertThat(BatchJobParameter.entries).allSatisfy {
            assertThat(it.description).isNotBlank()
        }
    }

    @Test
    fun `고를 수 있는 값이 있는 파라미터는 그 값만 받는다`() {
        assertThat(BatchJobParameter.TOPIC_TYPE.allowedValues).containsExactly("NOTICE", "MAJOR")
        assertThat(BatchJobParameter.RETENTION_DAYS.allowedValues).isNull()

        BatchJobParameter.entries.forEach { parameter ->
            parameter.allowedValues?.forEach {
                assertThatCode {
                    parameter.validate(it)
                }.doesNotThrowAnyException()
            }
        }
    }

    @Test
    fun `규칙에 맞는 파라미터는 통과한다`() {
        assertThatCode {
            BatchJob.NOTICE_CRAWL.validate(mapOf("topicType" to "NOTICE"))
            BatchJob.NOTICE_CRAWL.validate(mapOf("topicType" to "MAJOR"))
            BatchJob.MAINTENANCE.validate(mapOf("retentionDays" to "7"))
            BatchJob.DAILY_STATISTICS.validate(mapOf("lookbackDays" to "3", "apiLogRetentionDays" to "30"))
            BatchJob.SILENT_PUSH.validate(emptyMap())
        }.doesNotThrowAnyException()
    }

    @Test
    fun `필요한 파라미터가 없으면 실패한다`() {
        assertThatThrownBy {
            BatchJob.NOTICE_CRAWL.validate(emptyMap())
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("noticeCrawlJob 에는 topicType 파라미터가 필요합니다.")
    }

    @Test
    fun `받지 않는 파라미터가 있으면 실패한다`() {
        assertThatThrownBy {
            BatchJob.SILENT_PUSH.validate(mapOf("topicType" to "NOTICE"))
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("silentPushJob 에는 없는 파라미터입니다: topicType")
    }

    @Test
    fun `크롤링 게시판 유형은 NOTICE 와 MAJOR 만 받는다`() {
        listOf("MEAL", "notice", "").forEach { topicType ->
            assertThatThrownBy {
                BatchJob.NOTICE_CRAWL.validate(mapOf("topicType" to topicType))
            }.isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("topicType 은 NOTICE / MAJOR 중 하나여야 합니다")
        }
    }

    @Test
    fun `보존 일수는 1 이상의 정수만 받는다`() {
        listOf("0", "-1", "7일", "1.5").forEach { retentionDays ->
            assertThatThrownBy {
                BatchJob.MAINTENANCE.validate(mapOf("retentionDays" to retentionDays))
            }.isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("retentionDays 는 1 이상의 정수여야 합니다")
        }
    }

    @Test
    fun `일일 통계 Job 은 다시 집계할 일수와 API 로그 보존 일수를 받는다`() {
        assertThat(BatchJob.from("dailyStatisticsJob")).isEqualTo(BatchJob.DAILY_STATISTICS)
        assertThat(BatchJob.DAILY_STATISTICS.parameters)
            .containsExactly(BatchJobParameter.LOOKBACK_DAYS, BatchJobParameter.API_LOG_RETENTION_DAYS)
        assertThat(BatchJobParameter.LOOKBACK_DAYS.allowedValues).isNull()
        assertThat(BatchJobParameter.API_LOG_RETENTION_DAYS.allowedValues).isNull()

        assertThatThrownBy {
            BatchJob.DAILY_STATISTICS.validate(mapOf("lookbackDays" to "3"))
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("dailyStatisticsJob 에는 apiLogRetentionDays 파라미터가 필요합니다.")
    }

    @Test
    fun `다시 집계할 일수와 API 로그 보존 일수는 1 이상의 정수만 받는다`() {
        listOf("0", "-1", "3일", "1.5", "").forEach { days ->
            assertThatThrownBy {
                BatchJob.DAILY_STATISTICS.validate(mapOf("lookbackDays" to days, "apiLogRetentionDays" to "30"))
            }.isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("lookbackDays 는 1 이상의 정수여야 합니다")
            assertThatThrownBy {
                BatchJob.DAILY_STATISTICS.validate(mapOf("lookbackDays" to "3", "apiLogRetentionDays" to days))
            }.isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("apiLogRetentionDays 는 1 이상의 정수여야 합니다")
        }
    }

    @Test
    fun `다시 집계할 일수가 API 로그 보존 일수보다 작지 않으면 실패한다`() {
        assertThatCode {
            BatchJob.DAILY_STATISTICS.validate(mapOf("lookbackDays" to "29", "apiLogRetentionDays" to "30"))
        }.doesNotThrowAnyException()

        listOf("30", "31").forEach { lookbackDays ->
            assertThatThrownBy {
                BatchJob.DAILY_STATISTICS.validate(mapOf("lookbackDays" to lookbackDays, "apiLogRetentionDays" to "30"))
            }.isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("lookbackDays($lookbackDays) 는 apiLogRetentionDays(30) 보다 작아야 합니다")
        }
    }

}
