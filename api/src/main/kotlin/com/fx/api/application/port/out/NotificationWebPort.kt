package com.fx.api.application.port.out

import com.fx.global.domain.Notice
import com.fx.global.domain.TopicType

interface NotificationWebPort {

    fun pushTestNotice(fcmToken: String, topicType: TopicType, notice: Notice): Boolean

}