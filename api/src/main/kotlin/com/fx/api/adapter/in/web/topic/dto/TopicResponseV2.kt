package com.fx.api.adapter.`in`.web.topic.dto

import com.fx.common.domain.catalog.TopicView
import com.fx.common.domain.i18n.Language

/** v2 구독 토픽 조회. 토픽 객체 목록 (code 오름차순). */
data class TopicResponseV2(
    val subscribedTopics: List<TypeResponse>
) {

    companion object {

        fun from(subscribedTopics: List<TopicView>, language: Language): TopicResponseV2 =
            TopicResponseV2(TypeResponse.from(subscribedTopics.sortedBy {
                it.code
            }, language))
    }

}
