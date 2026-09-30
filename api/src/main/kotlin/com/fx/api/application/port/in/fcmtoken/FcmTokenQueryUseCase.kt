package com.fx.api.application.port.`in`.fcmtoken

import com.fx.common.domain.TopicType
import com.fx.common.domain.catalog.TopicView

interface FcmTokenQueryUseCase {

    /** 토큰이 구독한 [type] 토픽. code 오름차순. 삭제된 토픽은 뺀다. */
    fun getMyTopics(fcmToken: String, type: TopicType): List<TopicView>

}
