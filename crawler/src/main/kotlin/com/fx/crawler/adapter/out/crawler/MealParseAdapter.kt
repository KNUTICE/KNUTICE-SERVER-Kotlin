package com.fx.crawler.adapter.out.crawler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fx.crawler.appllication.port.out.MealParsePort
import com.fx.crawler.common.annotation.CrawlAdapter
import com.fx.global.domain.Meal
import com.fx.global.domain.MealType
import org.apache.commons.text.StringEscapeUtils
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate

/**
 * 식단 정보 Crawl & Parse 어뎁터입니다.
 *
 * @author 이동섭
 * @since 2025-10-21
 */
@CrawlAdapter
class MealParseAdapter(
) : MealParsePort {

    private val log = LoggerFactory.getLogger(MealParseAdapter::class.java)
    private val mapper = jacksonObjectMapper()

    override fun parseMeal(topic: MealType): Meal? {
        val today = LocalDate.now()

        val jsonResponse = WebClient.create().post()
            .uri(topic.getNoticeUrl())
            .retrieve()
            .bodyToMono(String::class.java)
            .block() ?: ""

        // Json 파싱
        val mealList: List<Map<String, Any>> = try {
            mapper.readValue(jsonResponse)
        } catch (e: Exception) {
            log.error("JSON 파싱 실패", e)
            emptyList()
        }

        // 오늘 날짜 메뉴 가져오기
        val todayMeal = mealList.firstOrNull { it["mealDate"] == today.toString() }
            ?: return null // 데이터가 없으면 즉시 null 반환하고 종료

        val koreaMenus = parseMenu(todayMeal["koreaFood"])
        val topMenus = parseMenu(todayMeal["topFood"])

        if (koreaMenus.isNullOrEmpty() && topMenus.isNullOrEmpty()) {
            return null
        }

        return Meal(
            mealDate = today,
            koreaMenus = koreaMenus,
            topMenus = topMenus,
            topic = topic
        )
    }

    // 문자열 식단 데이터를 리스트로 변환하는 로직
    private fun parseMenu(rawContent: Any?): List<String>? {
        val content = rawContent?.toString() ?: return null

        // 데이터가 "null" 문자열이거나 비어있는 경우 처리
        if (content.isBlank() || content.equals("null", ignoreCase = true)) {
            return null
        }

        return content.split("\r\n")
            .map { StringEscapeUtils.unescapeHtml4(it).trim() } // HTML 디코딩 (&lt; 와 같은 문자를 < 으로 표기하도록 함)
            .filter { it.isNotBlank() }
            .takeIf { it.isNotEmpty() }
    }
}