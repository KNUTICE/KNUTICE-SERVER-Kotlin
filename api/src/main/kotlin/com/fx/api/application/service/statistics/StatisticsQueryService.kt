package com.fx.api.application.service.statistics

import com.fx.api.application.port.`in`.statistics.StatisticsQueryUseCase
import com.fx.api.application.port.out.statistics.StatisticsPersistencePort
import com.fx.api.domain.DailyEndpointStatistics
import com.fx.api.domain.DailyStatistics
import com.fx.api.domain.DailyTokenStatistics
import com.fx.api.domain.DailyTopicStatistics
import com.fx.api.domain.DeviceCounts
import com.fx.api.domain.EndpointStatistics
import com.fx.api.domain.PeriodStatistics
import com.fx.api.domain.StatisticsPeriod
import com.fx.api.domain.TopicSubscriberCount
import com.fx.api.domain.TopicSubscriptionStatistics
import com.fx.api.domain.TopicViewStatistics
import com.fx.api.exception.StatisticsException
import com.fx.api.exception.errorcode.StatisticsErrorCode
import com.fx.common.application.port.`in`.CatalogQueryUseCase
import com.fx.common.domain.TopicType
import com.fx.common.domain.catalog.TopicCatalog
import com.fx.common.domain.statistics.StatisticsTime
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate

/** 일일 통계는 crawler 가 매일 새벽 어제까지 집계하므로 오늘 값은 없다. */
@Service
@Transactional(readOnly = true)
class StatisticsQueryService(
    private val statisticsPersistencePort: StatisticsPersistencePort,
    private val catalogQueryUseCase: CatalogQueryUseCase,
    private val clock: Clock,
) : StatisticsQueryUseCase {

    override fun getDailyStatistics(from: LocalDate?, to: LocalDate?): PeriodStatistics<DailyStatistics> =
        inPeriod(from, to) {
            statisticsPersistencePort.findDailyStatistics(it)
        }

    override fun getDailyTokenStatistics(from: LocalDate?, to: LocalDate?): PeriodStatistics<DailyTokenStatistics> =
        inPeriod(from, to) {
            statisticsPersistencePort.findDailyTokenStatistics(it)
        }

    override fun getEndpointStatistics(from: LocalDate?, to: LocalDate?): PeriodStatistics<EndpointStatistics> =
        inPeriod(from, to) {
            statisticsPersistencePort.findEndpointStatistics(it)
        }

    override fun getDailyEndpointStatistics(
        method: String,
        urlPattern: String,
        from: LocalDate?,
        to: LocalDate?,
    ): PeriodStatistics<DailyEndpointStatistics> =
        inPeriod(from, to) {
            statisticsPersistencePort.findDailyEndpointStatistics(method, urlPattern, it)
        }

    override fun getTopicStatistics(
        topicType: TopicType?,
        from: LocalDate?,
        to: LocalDate?,
    ): PeriodStatistics<TopicViewStatistics> =
        inPeriod(from, to) { period ->
            val catalog =
                catalogQueryUseCase.getTopicCatalog()
            val viewed = statisticsPersistencePort.findTopicRequestCounts(period).map {
                TopicViewStatistics(it.topicCode, it.topicName, catalog.findByCode(it.topicCode), it.requestCount)
            }

            (viewed + unviewedTopics(catalog, viewed))
                .filter {
                    topicType == null || it.topic?.topicType == topicType
                }
                .sortedWith(
                    compareByDescending<TopicViewStatistics> {
                        it.requestCount
                    }.thenBy {
                        it.topicCode
                    }
                )
        }

    override fun getDailyTopicStatistics(
        topicCode: Int,
        from: LocalDate?,
        to: LocalDate?,
    ): PeriodStatistics<DailyTopicStatistics> =
        inPeriod(from, to) {
            statisticsPersistencePort.findDailyTopicStatistics(topicCode, it)
        }

    /** 읽기 전용 트랜잭션 하나로 읽어 구독자 수와 활성 토큰 수가 같은 시점의 값이 되게 한다. */
    override fun getTopicSubscriptions(): TopicSubscriptionStatistics {
        val subscribers =
            statisticsPersistencePort.countSubscribersByTopic()

        return TopicSubscriptionStatistics(
            measuredAt = StatisticsTime.now(clock),
            activeTokens = statisticsPersistencePort.countActiveTokens(),
            topics = catalogQueryUseCase.getTopicCatalog().topics.map {
                TopicSubscriberCount(it, subscribers[it.code] ?: DeviceCounts.ZERO)
            },
        )
    }

    /** 아무도 보지 않는 게시판을 찾을 수 있도록, 앱에 보이는 공지 · 학과 토픽은 조회 기록이 없어도 0 으로 넣는다. */
    private fun unviewedTopics(catalog: TopicCatalog, viewed: List<TopicViewStatistics>): List<TopicViewStatistics> {
        val viewedCodes = viewed.mapTo(mutableSetOf()) {
            it.topicCode
        }
        return catalog.topics
            .filter {
                it.visible && it.topicType in NOTICE_LIST_TOPIC_TYPES && it.code !in viewedCodes
            }
            .map {
                TopicViewStatistics(it.code, it.name, it, 0)
            }
    }

    private fun <T> inPeriod(from: LocalDate?, to: LocalDate?, find: (StatisticsPeriod) -> List<T>): PeriodStatistics<T> {
        val period = try {
            StatisticsPeriod.of(from, to, StatisticsTime.today(clock))
        } catch (e: IllegalArgumentException) {
            throw StatisticsException(
                StatisticsErrorCode.STATISTICS_PERIOD_INVALID,
                e.message ?: StatisticsErrorCode.STATISTICS_PERIOD_INVALID.message,
                e,
            )
        }
        return PeriodStatistics(period, find(period))
    }

    companion object {

        /** 공지 목록을 조회하는 토픽 유형. 학식은 공지 목록 조회 대상이 아니다 */
        private val NOTICE_LIST_TOPIC_TYPES =
            setOf(TopicType.NOTICE, TopicType.MAJOR)

    }

}
