package com.fx.api.adapter.out.web

import com.fx.api.application.port.out.MealRemotePort
import com.fx.api.domain.Meal
import com.fx.common.annotation.hexagonal.WebOutputAdapter
import com.fx.common.domain.catalog.TopicView
import com.fx.common.exception.ConnectionException
import com.fx.common.exception.errorcode.ConnectionErrorCode
import org.apache.commons.text.StringEscapeUtils
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.readValue
import java.time.LocalDate

/**
 * 학교 식단 사이트 호출. 응답 Content-Type 에 기대지 않도록 문자열로 받아 직접 파싱한다.
 * 연결 · 읽기 타임아웃은 `spring.http.clients.*` 설정을 따른다.
 */
@WebOutputAdapter
class MealRemoteAdapter(
    restClientBuilder: RestClient.Builder,
    private val jsonMapper: JsonMapper,
) : MealRemotePort {

    private val restClient: RestClient = restClientBuilder.build()

    override fun getMeals(mealTopic: TopicView): List<Meal> {
        val body = try {
            restClient.get()
                .uri(mealTopic.noticeUrl())
                .retrieve()
                .body(String::class.java)
                .orEmpty()
        } catch (e: RestClientException) {
            throw ConnectionException(ConnectionErrorCode.REMOTE_SERVER_UNAVAILABLE)
        }

        return jsonMapper.readValue<List<MealResponseDto>>(body).mapNotNull(::toMeal)
    }

    private fun toMeal(dto: MealResponseDto): Meal? {
        val date = runCatching { dto.mealDate?.let(LocalDate::parse) }.getOrNull() ?: return null
        val koreaMenus = parseMenu(dto.koreaFood)
        val topMenus = parseMenu(dto.topFood)
        if (koreaMenus.isEmpty() && topMenus.isEmpty()) {
            return null
        }
        return Meal(mealDate = date, koreaMenus = koreaMenus, topMenus = topMenus)
    }

    /** 줄바꿈으로 구분된 메뉴 문자열을 목록으로 바꾸고 HTML 엔티티(`&lt;` 등)를 푼다. */
    private fun parseMenu(rawContent: String?): List<String> {
        val content = rawContent?.takeUnless { it.isBlank() || it.equals("null", ignoreCase = true) }
            ?: return emptyList()

        return content.split("\r\n")
            .map { StringEscapeUtils.unescapeHtml4(it).trim() }
            .filter { it.isNotBlank() }
    }

    private data class MealResponseDto(
        val mealDate: String?,
        val koreaFood: String?,
        val topFood: String?,
    )

}
