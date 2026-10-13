package com.fx.api.application.port.out.statistics

import com.fx.api.domain.DailyEndpointStatistics
import com.fx.api.domain.DailyStatistics
import com.fx.api.domain.DailyTokenStatistics
import com.fx.api.domain.DailyTopicStatistics
import com.fx.api.domain.DeviceCounts
import com.fx.api.domain.EndpointStatistics
import com.fx.api.domain.StatisticsPeriod
import com.fx.api.domain.TopicRequestCount

/**
 * crawler 가 집계한 일일 통계 테이블과 토큰 · 구독 현황을 읽는다.
 * 날짜별 결과는 집계된 날만 날짜순으로 담고, 집계는 됐는데 행이 없는 날은 0 으로 채운다.
 */
interface StatisticsPersistencePort {

    fun findDailyStatistics(period: StatisticsPeriod): List<DailyStatistics>

    fun findDailyTokenStatistics(period: StatisticsPeriod): List<DailyTokenStatistics>

    /** 엔드포인트별 기간 합계. 요청 수 내림차순, 같으면 URL 패턴 · 메서드 순 */
    fun findEndpointStatistics(period: StatisticsPeriod): List<EndpointStatistics>

    fun findDailyEndpointStatistics(method: String, urlPattern: String, period: StatisticsPeriod): List<DailyEndpointStatistics>

    /** 기간 안에 조회 기록이 있는 토픽만 */
    fun findTopicRequestCounts(period: StatisticsPeriod): List<TopicRequestCount>

    fun findDailyTopicStatistics(topicCode: Int, period: StatisticsPeriod): List<DailyTopicStatistics>

    /** 토픽 코드별 활성 토큰 구독자 수. 구독자가 없는 토픽은 결과에 없다 */
    fun countSubscribersByTopic(): Map<Int, DeviceCounts>

    fun countActiveTokens(): DeviceCounts

}
