package com.fx.api.adapter.out.web

import com.fx.api.application.port.out.MealRemotePort
import com.fx.global.annotation.hexagonal.WebOutputAdapter
import com.fx.global.domain.Meal
import com.fx.global.domain.MealType
import kotlinx.coroutines.reactor.awaitSingle
import org.apache.commons.text.StringEscapeUtils
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDate

@WebOutputAdapter
class MealRemoteAdapter(
    private val webClient: WebClient
) : MealRemotePort {

    override suspend fun getMeals(type: MealType): List<Meal> {
        val responses = webClient.get()
            .uri(type.rootDomain + type.bbsPath)
            .retrieve()
            .bodyToMono<List<MealResponseDto>>()
            .awaitSingle()
        return responses.mapNotNull { toDomain(it, type) }
    }

    private fun toDomain(mealResponseDto: MealResponseDto, type: MealType): Meal? {
        val date = runCatching {
            mealResponseDto.mealDate?.let { LocalDate.parse(it) }
        }.getOrNull() ?: return null

        val koreaMenus = parseMenu(mealResponseDto.koreaFood)
        val topMenus = parseMenu(mealResponseDto.topFood)

        if (koreaMenus.isNullOrEmpty() && topMenus.isNullOrEmpty()) {
            return null
        }

        return Meal(
            mealDate = date,
            koreaMenus = koreaMenus,
            topMenus = topMenus,
            topic = type
        )
    }

    private fun parseMenu(rawContent: String?): List<String>? {
        val content = rawContent ?: return null

        if (content.isBlank() || content.equals("null", ignoreCase = true)) {
            return null
        }

        return content.split("\r\n")
            .map { StringEscapeUtils.unescapeHtml4(it).trim() }
            .filter { it.isNotBlank() }
            .takeIf { it.isNotEmpty() }
    }

    private data class MealResponseDto(
        val mealDate: String?,
        val koreaFood: String?,
        val topFood: String?
    )

}
