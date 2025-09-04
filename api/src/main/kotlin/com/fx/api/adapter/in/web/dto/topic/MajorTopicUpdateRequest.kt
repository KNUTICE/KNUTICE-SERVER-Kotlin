package com.fx.api.adapter.`in`.web.dto.topic

import com.fx.api.application.port.`in`.dto.MajorTopicUpdateCommand
import com.fx.global.domain.MajorType

data class MajorTopicUpdateRequest(

    val type: MajorType,
    val enabled: Boolean

) {

    fun toCommand(fcmToken: String): MajorTopicUpdateCommand =
        MajorTopicUpdateCommand(
            fcmToken = fcmToken,
            type = this.type,
            enabled = this.enabled
        )

}
