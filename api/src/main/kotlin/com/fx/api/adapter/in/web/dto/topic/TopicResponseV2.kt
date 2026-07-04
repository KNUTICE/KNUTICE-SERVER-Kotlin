package com.fx.api.adapter.`in`.web.dto.topic

import com.fx.global.domain.CrawlableType

data class TopicResponseV2(
    val subscribedTopicIds: Set<Int>
) {
    companion object {
        fun from(subscribedTopics: Set<String>): TopicResponseV2 =
            TopicResponseV2(
                subscribedTopics.map { CrawlableType.fromString(it).code }.toSet()
            )
    }
}