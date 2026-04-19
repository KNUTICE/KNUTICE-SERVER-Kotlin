package com.fx.api.adapter.`in`.web.dto.topic

import com.fasterxml.jackson.annotation.JsonInclude
import com.fx.global.domain.MajorType
import com.fx.global.domain.MealType
import com.fx.global.domain.NoticeType
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

//@JsonInclude(JsonInclude.Include.NON_NULL) // dslee - null 인 필드는 응답에서 제외함
data class TypeResponse(
    val topic: String,
    val name: String,
    val college: String? = null
) {
    companion object {
        fun fromNoticeTypes(): List<TypeResponse> =
            NoticeType.entries.map { TypeResponse(topic = it.name, name = it.category) }

        fun fromMajorTypes(messageSource: MessageSource): List<TypeResponse> {
            return MajorType.entries.map {
                val locale = LocaleContextHolder.getLocale()
                TypeResponse(
                    topic = it.name,
                    name = messageSource.getMessage("topic.${it.name.lowercase()}", null, it.category, locale) ?: it.category,
                    college = messageSource.getMessage("college.${it.college.lowercase()}", null, it.college, locale) ?: it.college
                )
            }
        }

        fun fromMealTypes(): List<TypeResponse> =
            MealType.entries.map { TypeResponse(topic = it.name, name = it.category) }
    }
}