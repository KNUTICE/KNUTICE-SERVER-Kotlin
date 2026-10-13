package com.fx.api.adapter.`in`.web.statistics

import com.fx.api.adapter.`in`.web.statistics.dto.DailyStatisticsResponse
import com.fx.api.adapter.`in`.web.statistics.dto.EndpointDailyStatisticsResponse
import com.fx.api.adapter.`in`.web.statistics.dto.EndpointStatisticsResponse
import com.fx.api.adapter.`in`.web.statistics.dto.TokenStatisticsResponse
import com.fx.api.adapter.`in`.web.statistics.dto.TopicDailyStatisticsResponse
import com.fx.api.adapter.`in`.web.statistics.dto.TopicStatisticsResponse
import com.fx.api.adapter.`in`.web.statistics.dto.TopicSubscriptionResponse
import com.fx.common.domain.TopicType
import io.github.seob7.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate

@Tag(
    name = "통계 API - ADMIN",
    description = "기간(from · to)은 KST 날짜이고 양 끝을 포함합니다. 없으면 어제까지 30일, 최대 366일이며 넘으면 STATISTICS_PERIOD_INVALID(400)입니다. " +
            "통계는 어제까지 집계되므로 오늘 값은 없고, 날짜별 결과에는 집계된 날만 날짜순으로 들어갑니다."
)
interface StatisticsApiSwagger {

    @Operation(summary = "일별 요약", description = "날짜별 요청 · 기기 · 공지 · 요약 · 신규 토큰 수")
    fun getDailyStatistics(
        @Parameter(description = FROM) @RequestParam(required = false) from: LocalDate?,
        @Parameter(description = TO) @RequestParam(required = false) to: LocalDate?,
    ): ResponseEntity<Api<DailyStatisticsResponse>>

    @Operation(
        summary = "일별 토큰 현황",
        description = "날짜별 활성 · 비활성 토큰 수와 신규 토큰 수. 일일 통계 Job 이 현황을 기록하지 못한 날은 active · inactive 가 null 입니다."
    )
    fun getTokenStatistics(
        @Parameter(description = FROM) @RequestParam(required = false) from: LocalDate?,
        @Parameter(description = TO) @RequestParam(required = false) to: LocalDate?,
    ): ResponseEntity<Api<TokenStatisticsResponse>>

    @Operation(summary = "엔드포인트별 기간 합계", description = "엔드포인트(메서드 + URL 패턴)별 요청 · 오류 수와 처리 시간. 요청 수 내림차순")
    fun getEndpointStatistics(
        @Parameter(description = FROM) @RequestParam(required = false) from: LocalDate?,
        @Parameter(description = TO) @RequestParam(required = false) to: LocalDate?,
    ): ResponseEntity<Api<EndpointStatisticsResponse>>

    @Operation(summary = "엔드포인트 일별 추이", description = "엔드포인트 하나의 날짜별 요청 · 오류 · 처리 시간 · 기기 수. 요청이 없던 날은 0")
    fun getEndpointDailyStatistics(
        @Parameter(description = "HTTP 메서드 (예: GET)") @RequestParam method: String,
        @Parameter(description = "URL 패턴 (예: /open-api/v1/notices/{nttId})") @RequestParam urlPattern: String,
        @Parameter(description = FROM) @RequestParam(required = false) from: LocalDate?,
        @Parameter(description = TO) @RequestParam(required = false) to: LocalDate?,
    ): ResponseEntity<Api<EndpointDailyStatisticsResponse>>

    @Operation(
        summary = "토픽별 조회 수",
        description = "토픽별 공지 목록 조회 수(v1 + v2). 조회 수 내림차순이고, 앱에 보이는 공지 · 학과 토픽은 조회가 없어도 0 으로 나옵니다."
    )
    fun getTopicStatistics(
        @Parameter(description = "토픽 유형. 없으면 모든 유형") @RequestParam(required = false) type: TopicType?,
        @Parameter(description = FROM) @RequestParam(required = false) from: LocalDate?,
        @Parameter(description = TO) @RequestParam(required = false) to: LocalDate?,
    ): ResponseEntity<Api<TopicStatisticsResponse>>

    @Operation(summary = "토픽 일별 조회 수", description = "토픽 하나의 날짜별 공지 목록 조회 수. 조회가 없던 날은 0")
    fun getTopicDailyStatistics(
        @Parameter(description = "토픽 코드 (v2 topicId)") @PathVariable topicCode: Int,
        @Parameter(description = FROM) @RequestParam(required = false) from: LocalDate?,
        @Parameter(description = TO) @RequestParam(required = false) to: LocalDate?,
    ): ResponseEntity<Api<TopicDailyStatisticsResponse>>

    @Operation(summary = "토픽별 구독자 수", description = "지금 시점에 토픽마다 알림을 받는 활성 토큰 수와 활성 토큰 전체 수")
    fun getTopicSubscriptions(): ResponseEntity<Api<TopicSubscriptionResponse>>

    companion object {
        private const val FROM = "시작 날짜 (yyyy-MM-dd, 포함). 없으면 끝 날짜까지 30일"
        private const val TO = "끝 날짜 (yyyy-MM-dd, 포함). 없으면 어제"
    }

}
