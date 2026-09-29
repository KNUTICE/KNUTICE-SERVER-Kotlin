package com.fx.migration.legacy

import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

class LegacyDocumentTest {

    private val seoul = ZoneId.of("Asia/Seoul")

    @Test
    fun `레거시가 전날 15시(UTC)로 저장한 날짜를 원래 날짜로 되돌린다`() {
        val document = LegacyDocument(Document("registrationDate", Date.from(Instant.parse("2026-09-28T15:00:00Z"))), seoul)

        assertThat(document.date("registrationDate")).isEqualTo(LocalDate.of(2026, 9, 29))
    }

    @Test
    fun `레거시가 9시간 앞선 UTC 로 저장한 시각을 원래 시각으로 되돌린다`() {
        val document = LegacyDocument(Document("createdAt", Date.from(Instant.parse("2026-09-29T01:00:00Z"))), seoul)

        assertThat(document.dateTime("createdAt")).isEqualTo(LocalDateTime.of(2026, 9, 29, 10, 0))
    }

    @Test
    fun `문자열 날짜 · 없는 값 · 배열 · 불리언 이름을 처리한다`() {
        val document = LegacyDocument(
            Document("registrationDate", "2026-09-29")
                .append("topics", listOf("GENERAL_NEWS", "SCHOLARSHIP_NEWS"))
                .append("attachment", true),
            seoul,
        )

        assertThat(document.date("registrationDate")).isEqualTo(LocalDate.of(2026, 9, 29))
        assertThat(document.dateTime("createdAt")).isNull()
        assertThat(document.strings("topics")).containsExactly("GENERAL_NEWS", "SCHOLARSHIP_NEWS")
        assertThat(document.strings("missing")).isEmpty()
        assertThat(document.boolean("isAttachment", "attachment")).isTrue()
    }

}
