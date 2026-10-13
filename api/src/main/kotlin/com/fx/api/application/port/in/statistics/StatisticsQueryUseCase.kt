package com.fx.api.application.port.`in`.statistics

import com.fx.api.domain.DailyEndpointStatistics
import com.fx.api.domain.DailyStatistics
import com.fx.api.domain.DailyTokenStatistics
import com.fx.api.domain.DailyTopicStatistics
import com.fx.api.domain.EndpointStatistics
import com.fx.api.domain.PeriodStatistics
import com.fx.api.domain.TopicSubscriptionStatistics
import com.fx.api.domain.TopicViewStatistics
import com.fx.common.domain.TopicType
import java.time.LocalDate

/**
 * crawler 의 일일 통계 Job 이 집계한 통계 조회.
 *
 * 기간은 KST 날짜이고 양 끝을 포함한다. [from] · [to] 가 없으면 어제까지 30일이다.
 * 날짜별 결과는 집계된 날만 날짜순으로 담는다. 집계는 됐는데 요청이 없던 날은 0 이다.
 * 기간이 잘못되면 `StatisticsException`(STATISTICS_PERIOD_INVALID)이 발생한다.
 */
interface StatisticsQueryUseCase {

    fun getDailyStatistics(from: LocalDate?, to: LocalDate?): PeriodStatistics<DailyStatistics>

    fun getDailyTokenStatistics(from: LocalDate?, to: LocalDate?): PeriodStatistics<DailyTokenStatistics>

    /** 엔드포인트별 기간 합계. 요청 수 내림차순 */
    fun getEndpointStatistics(from: LocalDate?, to: LocalDate?): PeriodStatistics<EndpointStatistics>

    fun getDailyEndpointStatistics(
        method: String,
        urlPattern: String,
        from: LocalDate?,
        to: LocalDate?,
    ): PeriodStatistics<DailyEndpointStatistics>

    /**
     * 토픽별 공지 목록 조회 수의 기간 합계. 조회 수 내림차순, 같으면 토픽 코드 순.
     * 앱에 보이는 공지 · 학과 토픽은 조회가 없어도 0 으로 담는다. [topicType] 을 주면 그 유형만 (삭제된 토픽은 빠진다).
     */
    fun getTopicStatistics(topicType: TopicType?, from: LocalDate?, to: LocalDate?): PeriodStatistics<TopicViewStatistics>

    /** 삭제된 토픽도 조회할 수 있다. 모르는 토픽 코드면 모든 날이 0 이다. */
    fun getDailyTopicStatistics(topicCode: Int, from: LocalDate?, to: LocalDate?): PeriodStatistics<DailyTopicStatistics>

    /** 지금 시점의 토픽별 활성 구독자 수. 카탈로그의 토픽 전체 */
    fun getTopicSubscriptions(): TopicSubscriptionStatistics

}
