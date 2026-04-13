package com.fx.api.adapter.`in`.web.dto.topic

import com.fasterxml.jackson.annotation.JsonInclude
import com.fx.global.domain.MajorType
import com.fx.global.domain.MealType
import com.fx.global.domain.NoticeType

//@JsonInclude(JsonInclude.Include.NON_NULL) // dslee - null 인 필드는 응답에서 제외함
data class TypeResponse(
    val topic: String,
    val name: String,
    val college: String? = null
) {
    companion object {
        fun fromNoticeTypes(): List<TypeResponse> =
            NoticeType.entries.map { TypeResponse(topic = it.name, name = it.category) }

        fun fromMajorTypes(): List<TypeResponse> =
            MajorType.entries.map { TypeResponse(topic = it.name, name = it.category, college = it.college) }

        fun fromMealTypes(): List<TypeResponse> =
            MealType.entries.map { TypeResponse(topic = it.name, name = it.category) }
    }
}