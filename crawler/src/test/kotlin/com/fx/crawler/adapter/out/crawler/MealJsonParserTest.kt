package com.fx.crawler.adapter.out.crawler

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tools.jackson.databind.json.JsonMapper
import java.time.LocalDate

class MealJsonParserTest {

    private val parser = MealJsonParser(JsonMapper.builder().build())

    private val json = """
        [
          {"mealDate":"2026-09-30","koreaFood":"김치찌개\r\n&lt;특식&gt; 제육볶음\r\n\r\n","topFood":"null","mealSeq":1},
          {"mealDate":"2026-10-01","koreaFood":"","topFood":null}
        ]
    """.trimIndent()

    @Test
    fun `해당 날짜 식단을 읽고 HTML 엔티티를 푼다`() {
        val meal = parser.parse(json, topicCode = 900, date = LocalDate.of(2026, 9, 30))

        assertThat(meal).isNotNull
        assertThat(meal!!.topicCode).isEqualTo(900)
        assertThat(meal.koreaMenus).containsExactly("김치찌개", "<특식> 제육볶음")
        assertThat(meal.topMenus).isEmpty()
    }

    @Test
    fun `메뉴가 모두 비어 있으면 null`() {
        assertThat(parser.parse(json, 900, LocalDate.of(2026, 10, 1))).isNull()
    }

    @Test
    fun `해당 날짜 식단이 없으면 null`() {
        assertThat(parser.parse(json, 900, LocalDate.of(2026, 10, 2))).isNull()
    }

}
