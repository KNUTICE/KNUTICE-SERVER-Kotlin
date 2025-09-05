package com.fx.crawler.adapter.out.crawler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fx.crawler.appllication.port.out.MealParsePort
import com.fx.crawler.common.annotation.CrawlAdapter
import com.fx.global.domain.Meal
import com.fx.global.domain.MealType
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate

@CrawlAdapter
class MealParseAdapter(
) : MealParsePort {

    private val log = LoggerFactory.getLogger(MealParseAdapter::class.java)
    private val mapper = jacksonObjectMapper()

    override fun parseMeal(type: MealType): Meal {
        val today = LocalDate.now()

        val jsonResponse = WebClient.create().post()
            .uri(type.getNoticeUrl())
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

        val menus = when (type) {
            MealType.STUDENT_CAFETERIA -> todayMeal?.get("topFood")?.toString()
            MealType.STAFF_CAFETERIA -> todayMeal?.get("koreaFood")?.toString()
        }?.split("\r\n")
            ?.filter { it.isNotBlank() }
            ?: emptyList()

        return Meal(
            mealDate = today,
            menus = menus,
            type = type
        )
    }
}