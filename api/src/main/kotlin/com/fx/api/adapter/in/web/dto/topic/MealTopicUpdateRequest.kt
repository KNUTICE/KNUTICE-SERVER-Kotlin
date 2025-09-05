package com.fx.api.adapter.`in`.web.dto.topic

import com.fx.api.application.port.`in`.dto.topic.MealTopicUpdateCommand
import com.fx.global.domain.MealType

data class MealTopicUpdateRequest(

    val type: MealType,
    val enabled: Boolean

) {

    fun toCommand(fcmToken: String): MealTopicUpdateCommand =
        MealTopicUpdateCommand(
            fcmToken = fcmToken,
            type = this.type,
            enabled = this.enabled
        )

}