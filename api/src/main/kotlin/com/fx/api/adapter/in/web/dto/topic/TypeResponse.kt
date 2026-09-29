package com.fx.api.adapter.`in`.web.dto.topic

import com.fx.common.domain.catalog.TopicView
import com.fx.common.domain.i18n.Language

/**
 * 토픽 한 건. 표시명 · 단과대 이름은 요청 언어로 해석하고 번역이 없으면 한국어를 쓴다.
 * 단과대는 학과 토픽에만 있고, 없으면 `null` 을 그대로 내보낸다.
 */
data class TypeResponse(
    val topic: String,
    val topicId: Int,
    val name: String,
    val college: String? = null
) {

    companion object {

        fun from(topic: TopicView, language: Language): TypeResponse =
            TypeResponse(
                topic = topic.name,
                topicId = topic.code,
                name = topic.displayName.resolve(language),
                college = topic.college?.displayName?.resolve(language)
            )

        fun from(topics: List<TopicView>, language: Language): List<TypeResponse> =
            topics.map { from(it, language) }
    }

}
