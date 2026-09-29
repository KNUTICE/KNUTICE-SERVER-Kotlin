package com.fx.common.domain.batch

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class BatchJobParametersTest {

    @Test
    fun `문자열 값만 가진 JSON 객체를 읽는다`() {
        assertThat(BatchJobParameters.parse("""{"topicType":"MAJOR"}""")).isEqualTo(mapOf("topicType" to "MAJOR"))
        assertThat(BatchJobParameters.parse("{}")).isEmpty()
    }

    @Test
    fun `JSON 객체가 아니면 거절한다`() {
        assertThatThrownBy { BatchJobParameters.parse("""["MAJOR"]""") }.isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { BatchJobParameters.parse("topicType=MAJOR") }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `문자열이 아닌 값은 거절한다`() {
        assertThatThrownBy { BatchJobParameters.parse("""{"retentionDays":30}""") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("retentionDays")
    }

    @Test
    fun `폴러가 붙이는 이름은 쓸 수 없다`() {
        assertThatThrownBy { BatchJobParameters.parse("""{"scheduledAt":"2026-09-30T10:00"}""") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("예약된")
    }

    @Test
    fun `읽은 값을 다시 JSON 으로 쓴다`() {
        val parameters = mapOf("topicType" to "NOTICE")

        assertThat(BatchJobParameters.parse(BatchJobParameters.format(parameters))).isEqualTo(parameters)
    }

}
