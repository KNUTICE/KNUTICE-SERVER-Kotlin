package com.fx.api.application.port.`in`.dto.topic

import com.fx.global.domain.MealType

data class MealTopicUpdateCommand (
    val fcmToken: String,
    val type: MealType,
    val enabled: Boolean
)