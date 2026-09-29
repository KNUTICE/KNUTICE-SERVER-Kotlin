package com.fx.common.domain.batch

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class BatchCronTest {

    @Test
    fun `다음 발화 시각을 계산한다`() {
        val cron = BatchCron.parse("0 0/15 * * * *")

        assertThat(cron.next(LocalDateTime.of(2026, 9, 30, 10, 7, 30)))
            .isEqualTo(LocalDateTime.of(2026, 9, 30, 10, 15))
        assertThat(cron.next(LocalDateTime.of(2026, 9, 30, 10, 15)))
            .isEqualTo(LocalDateTime.of(2026, 9, 30, 10, 30))
    }

    @Test
    fun `요일 이름을 쓸 수 있다`() {
        // 2026-10-03 은 토요일
        assertThat(BatchCron.parse("0 10 10 * * MON-FRI").next(LocalDateTime.of(2026, 10, 2, 11, 0)))
            .isEqualTo(LocalDateTime.of(2026, 10, 5, 10, 10))
    }

    @Test
    fun `초 필드가 0 이 아니면 거절한다`() {
        assertThatThrownBy { BatchCron.parse("*/30 * * * * *") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("초 필드")
    }

    @Test
    fun `6개 필드가 아니면 거절한다`() {
        assertThatThrownBy { BatchCron.parse("0 * * * *") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `형식이 잘못되면 거절한다`() {
        assertThatThrownBy { BatchCron.parse("0 61 * * * *") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

}
