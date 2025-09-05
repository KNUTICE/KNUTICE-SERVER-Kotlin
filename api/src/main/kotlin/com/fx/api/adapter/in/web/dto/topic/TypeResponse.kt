package com.fx.api.adapter.`in`.web.dto.topic

import com.fx.global.domain.MajorType
import com.fx.global.domain.MealType
import com.fx.global.domain.NoticeType

data class TypeResponse(
    val topic: String,
    val name: String
) {
    companion object {
        fun fromNoticeTypes(): List<TypeResponse> =
            NoticeType.entries.map { TypeResponse(topic = it.name, name = it.category) }

        fun fromMajorTypes(): List<TypeResponse> =
            MajorType.entries.map { TypeResponse(topic = it.name, name = it.category) }

        fun fromMealTypes(): List<TypeResponse> =
            MealType.entries.map { TypeResponse(topic = it.name, name = it.category) }
    }
}