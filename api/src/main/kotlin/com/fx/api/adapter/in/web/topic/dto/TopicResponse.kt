package com.fx.api.adapter.`in`.web.topic.dto

import com.fx.common.domain.catalog.TopicView

/** v1 구독 토픽 조회. 토픽 이름 목록 (code 오름차순). */
data class TopicResponse(
    val subscribedTopics: Set<String>
) {

    companion object {

        fun from(subscribedTopics: List<TopicView>): TopicResponse =
            TopicResponse(subscribedTopics.mapTo(linkedSetOf()) {
                it.name
            })
    }

}
