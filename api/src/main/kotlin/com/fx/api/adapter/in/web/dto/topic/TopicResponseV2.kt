package com.fx.api.adapter.`in`.web.dto.topic

import com.fx.global.domain.CrawlableType
import org.springframework.context.MessageSource

data class TopicResponseV2(
    val subscribedTopics: List<TypeResponse>
) {
    companion object {
        fun from(subscribedTopics: Set<String>, messageSource: MessageSource): TopicResponseV2 {
            val types = subscribedTopics
                .map { CrawlableType.fromString(it) }
                .sortedBy { it.code }

            return TopicResponseV2(
                subscribedTopics = types.map { TypeResponse.from(it, messageSource) }
            )
        }
    }
}
