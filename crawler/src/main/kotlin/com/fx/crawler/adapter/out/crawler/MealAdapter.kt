package com.fx.crawler.adapter.out.crawler

import com.fx.common.domain.catalog.TopicView
import com.fx.crawler.application.port.out.MealPort
import com.fx.crawler.common.annotation.CrawlAdapter
import com.fx.crawler.domain.meal.Meal
import org.springframework.web.client.RestClient
import tools.jackson.databind.json.JsonMapper
import java.net.URI
import java.time.LocalDate

/** 학교 식단 사이트 조회. 연결 · 읽기 타임아웃은 `spring.http.clients.*` 설정을 따른다. */
@CrawlAdapter
class MealAdapter(
    restClientBuilder: RestClient.Builder,
    jsonMapper: JsonMapper,
) : MealPort {

    private val restClient: RestClient = restClientBuilder.build()
    private val parser = MealJsonParser(jsonMapper)

    override fun fetchMeal(topic: TopicView, date: LocalDate): Meal? {
        val body = restClient.post()
            .uri(URI.create(topic.noticeUrl()))
            .retrieve()
            .body(ByteArray::class.java)
            ?: return null
        return parser.parse(String(body, Charsets.UTF_8), topic.code, date)
    }

}
