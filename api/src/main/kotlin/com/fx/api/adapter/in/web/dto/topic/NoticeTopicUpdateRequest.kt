package com.fx.api.adapter.`in`.web.dto.topic

import com.fx.api.application.port.`in`.dto.NoticeTopicUpdateCommand
import com.fx.global.domain.NoticeType

data class NoticeTopicUpdateRequest(

    val type: NoticeType,
    val enabled: Boolean

) {

    fun toCommand(fcmToken: String): NoticeTopicUpdateCommand =
        NoticeTopicUpdateCommand(
            fcmToken = fcmToken,
            type = this.type,
            enabled = this.enabled
        )

}