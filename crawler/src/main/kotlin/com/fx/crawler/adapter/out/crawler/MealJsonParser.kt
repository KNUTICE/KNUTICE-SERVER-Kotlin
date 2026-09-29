package com.fx.crawler.adapter.out.crawler

import com.fx.crawler.domain.meal.Meal
import org.apache.commons.text.StringEscapeUtils
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.readValue
import java.time.LocalDate

/**
 * 학교 식단 사이트 응답(JSON 배열) 파서. 메뉴는 줄바꿈(`\r\n`)으로 구분된 문자열이고 HTML 엔티티(`&lt;` 등)가 섞여 있다.
 */
class MealJsonParser(
    private val jsonMapper: JsonMapper,
) {

    /** [date] 의 식단. 없거나 한식 · 일품 메뉴가 모두 비어 있으면 null. */
    fun parse(json: String, topicCode: Int, date: LocalDate): Meal? {
        val meal = jsonMapper.readValue<List<MealResponse>>(json)
            .firstOrNull { it.mealDate == date.toString() }
            ?: return null

        val koreaMenus = parseMenu(meal.koreaFood)
        val topMenus = parseMenu(meal.topFood)
        if (koreaMenus.isEmpty() && topMenus.isEmpty()) {
            return null
        }
        return Meal(topicCode = topicCode, mealDate = date, koreaMenus = koreaMenus, topMenus = topMenus)
    }

    private fun parseMenu(rawContent: String?): List<String> {
        val content = rawContent?.takeUnless { it.isBlank() || it.equals("null", ignoreCase = true) }
            ?: return emptyList()

        return content.split("\r\n")
            .map { StringEscapeUtils.unescapeHtml4(it).trim() }
            .filter { it.isNotBlank() }
    }

    private data class MealResponse(
        val mealDate: String?,
        val koreaFood: String?,
        val topFood: String?,
    )

}
