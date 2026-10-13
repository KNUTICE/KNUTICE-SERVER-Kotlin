package com.fx.api.adapter.`in`.web.statistics

import com.fx.api.adapter.`in`.web.statistics.dto.DailyStatisticsResponse
import com.fx.api.adapter.`in`.web.statistics.dto.EndpointDailyStatisticsResponse
import com.fx.api.adapter.`in`.web.statistics.dto.EndpointStatisticsResponse
import com.fx.api.adapter.`in`.web.statistics.dto.TokenStatisticsResponse
import com.fx.api.adapter.`in`.web.statistics.dto.TopicDailyStatisticsResponse
import com.fx.api.adapter.`in`.web.statistics.dto.TopicStatisticsResponse
import com.fx.api.adapter.`in`.web.statistics.dto.TopicSubscriptionResponse
import com.fx.api.application.port.`in`.statistics.StatisticsQueryUseCase
import com.fx.common.annotation.hexagonal.WebInputAdapter
import com.fx.common.domain.TopicType
import io.github.seob7.Api
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate

@WebInputAdapter
@RequestMapping("/api/v1/statistics")
class StatisticsApiAdapter(
    private val statisticsQueryUseCase: StatisticsQueryUseCase,
) : StatisticsApiSwagger {

    @GetMapping("/daily")
    override fun getDailyStatistics(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate?,
    ): ResponseEntity<Api<DailyStatisticsResponse>> =
        Api.OK(DailyStatisticsResponse.from(statisticsQueryUseCase.getDailyStatistics(from, to)), "일별 통계 조회 성공")

    @GetMapping("/tokens")
    override fun getTokenStatistics(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate?,
    ): ResponseEntity<Api<TokenStatisticsResponse>> =
        Api.OK(TokenStatisticsResponse.from(statisticsQueryUseCase.getDailyTokenStatistics(from, to)), "토큰 현황 조회 성공")

    @GetMapping("/endpoints")
    override fun getEndpointStatistics(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate?,
    ): ResponseEntity<Api<EndpointStatisticsResponse>> =
        Api.OK(EndpointStatisticsResponse.from(statisticsQueryUseCase.getEndpointStatistics(from, to)), "엔드포인트 통계 조회 성공")

    @GetMapping("/endpoints/daily")
    override fun getEndpointDailyStatistics(
        @RequestParam method: String,
        @RequestParam urlPattern: String,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate?,
    ): ResponseEntity<Api<EndpointDailyStatisticsResponse>> {
        val statistics =
            statisticsQueryUseCase.getDailyEndpointStatistics(method, urlPattern, from, to)
        return Api.OK(EndpointDailyStatisticsResponse.from(method, urlPattern, statistics), "엔드포인트 일별 통계 조회 성공")
    }

    @GetMapping("/topics")
    override fun getTopicStatistics(
        @RequestParam(required = false) type: TopicType?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate?,
    ): ResponseEntity<Api<TopicStatisticsResponse>> =
        Api.OK(TopicStatisticsResponse.from(statisticsQueryUseCase.getTopicStatistics(type, from, to)), "토픽 통계 조회 성공")

    @GetMapping("/topics/{topicCode}/daily")
    override fun getTopicDailyStatistics(
        @PathVariable topicCode: Int,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate?,
    ): ResponseEntity<Api<TopicDailyStatisticsResponse>> {
        val statistics =
            statisticsQueryUseCase.getDailyTopicStatistics(topicCode, from, to)
        return Api.OK(TopicDailyStatisticsResponse.from(topicCode, statistics), "토픽 일별 통계 조회 성공")
    }

    @GetMapping("/subscriptions")
    override fun getTopicSubscriptions(): ResponseEntity<Api<TopicSubscriptionResponse>> =
        Api.OK(TopicSubscriptionResponse.from(statisticsQueryUseCase.getTopicSubscriptions()), "구독자 수 조회 성공")

}
