package com.fx.api.adapter.`in`.web.dto.topic

import com.fasterxml.jackson.annotation.JsonInclude
import com.fx.global.domain.CrawlableType
import com.fx.global.domain.MajorType
import com.fx.global.domain.MealType
import com.fx.global.domain.NoticeType
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

//@JsonInclude(JsonInclude.Include.NON_NULL) // dslee - null 인 필드는 응답에서 제외함
data class TypeResponse(
    val topic: String,
    val topicId: Int,
    val name: String,
    val college: String? = null
) {
    companion object {
        fun fromNoticeTypes(): List<TypeResponse> =
            NoticeType.entries.map { TypeResponse(topic = it.name, topicId = it.code, name = it.category) }

        fun fromMajorTypes(messageSource: MessageSource): List<TypeResponse> {
            return MajorType.entries.map {
                val locale = LocaleContextHolder.getLocale()
                TypeResponse(
                    topic = it.name,
                    topicId = it.code,
                    name = messageSource.getMessage("topic.${it.name.lowercase()}", null, it.category, locale) ?: it.category,
                    college = messageSource.getMessage("college.${it.college.lowercase()}", null, it.college, locale) ?: it.college
                )
            }
        }

        fun fromMealTypes(): List<TypeResponse> =
            MealType.entries.map { TypeResponse(topic = it.name, topicId = it.code, name = it.category) }

        fun from(type: CrawlableType, messageSource: MessageSource): TypeResponse =
            when (type) {
                is MajorType -> {
                    val locale = LocaleContextHolder.getLocale()
                    TypeResponse(
                        topic = type.topicName,
                        topicId = type.code,
                        name = messageSource.getMessage("topic.${type.name.lowercase()}", null, type.category, locale) ?: type.category,
                        college = messageSource.getMessage("college.${type.college.lowercase()}", null, type.college, locale) ?: type.college
                    )
                }
                else -> TypeResponse(topic = type.topicName, topicId = type.code, name = type.category)
            }
    }
}