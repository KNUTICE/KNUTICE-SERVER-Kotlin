package com.fx.api.adapter.`in`.web.statistics.dto

import com.fx.api.domain.TopicSubscriberCount
import com.fx.api.domain.TopicSubscriptionStatistics
import com.fx.common.domain.TopicType
import com.fx.common.domain.i18n.Language
import java.time.LocalDateTime

data class TopicSubscriptionResponse(
    /** 조회한 시각 */
    val measuredAt: LocalDateTime,
    /** 활성 토큰 전체 수. 구독률의 분모 */
    val activeTokenCount: Long,
    val activeIosTokenCount: Long,
    val activeAosTokenCount: Long,
    /** 삭제되지 않은 토픽 전체 (토픽 코드 순) */
    val topics: List<TopicResponse>,
) {

    data class TopicResponse(
        /** 토픽 코드 (v2 topicId) */
        val topicCode: Int,
        /** 토픽 이름 (v1 topic) */
        val topicName: String,
        val topicType: TopicType,
        /** 한국어 표시명 */
        val displayName: String,
        /** 학과 토픽의 단과대 한국어 표시명. 그 밖의 토픽은 null */
        val college: String?,
        /** 앱 노출 여부. 숨긴 토픽에도 기존 구독이 남아 있을 수 있다 */
        val visible: Boolean,
        /** 이 토픽을 구독한 활성 토큰 수 (= 발송 대상 수) */
        val subscriberCount: Long,
        val iosSubscriberCount: Long,
        val aosSubscriberCount: Long,
    ) {

        companion object {

            fun from(count: TopicSubscriberCount): TopicResponse =
                TopicResponse(
                    topicCode = count.topic.code,
                    topicName = count.topic.name,
                    topicType = count.topic.topicType,
                    displayName = count.topic.displayName.resolve(Language.KO),
                    college = count.topic.college?.displayName?.resolve(Language.KO),
                    visible = count.topic.visible,
                    subscriberCount = count.subscribers.total,
                    iosSubscriberCount = count.subscribers.ios,
                    aosSubscriberCount = count.subscribers.aos,
                )

        }

    }

    companion object {

        fun from(statistics: TopicSubscriptionStatistics): TopicSubscriptionResponse =
            TopicSubscriptionResponse(
                measuredAt = statistics.measuredAt,
                activeTokenCount = statistics.activeTokens.total,
                activeIosTokenCount = statistics.activeTokens.ios,
                activeAosTokenCount = statistics.activeTokens.aos,
                topics = statistics.topics.map {
                    TopicResponse.from(it)
                },
            )

    }

}
