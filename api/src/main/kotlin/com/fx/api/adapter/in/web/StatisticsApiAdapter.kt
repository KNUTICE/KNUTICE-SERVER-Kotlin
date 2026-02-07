package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.dto.statistics.ApiLogStatisticsResponse
import com.fx.api.adapter.`in`.web.dto.statistics.StatisticsResponse
import com.fx.api.adapter.`in`.web.dto.statistics.TopicCountStatisticsResponse
import com.fx.api.adapter.`in`.web.swagger.StatisticsApiSwagger
import com.fx.api.application.port.`in`.StatisticsQueryUseCase
import com.fx.global.annotation.hexagonal.WebInputAdapter
import io.github.seob7.Api
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate

@WebInputAdapter
@RequestMapping("/api/v1/statistics")
class StatisticsApiAdapter(
    private val statisticsQueryUseCase: StatisticsQueryUseCase
) : StatisticsApiSwagger {

    @GetMapping()
    override suspend fun getDailyStatistics(
        @RequestParam(required = false) cursorDate: LocalDate?,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Api<List<StatisticsResponse>>> =
        Api.OK(StatisticsResponse.from(statisticsQueryUseCase.getDailyStatistics(cursorDate, size)))

    // TODO : Swagger 문서화
    @GetMapping("/api-logs")
    suspend fun getApiLogStatistics(
        @RequestParam(required = false) cursorDate: LocalDate?,
        @RequestParam(defaultValue = "7") size: Int
    ): ResponseEntity<Api<List<ApiLogStatisticsResponse>>> =
        Api.OK(ApiLogStatisticsResponse.from(statisticsQueryUseCase.getApiLogStatistics(cursorDate, size)))

    @GetMapping("/topics")
    suspend fun getTopicStatistics(
        @RequestParam(required = false) cursorDate: LocalDate?,
        @RequestParam(defaultValue = "7") size: Int
    ): ResponseEntity<Api<List<TopicCountStatisticsResponse>>> =
        Api.OK(
            TopicCountStatisticsResponse.from(
                statisticsQueryUseCase.getTopicStatistics(cursorDate, size)
            )
        )

}