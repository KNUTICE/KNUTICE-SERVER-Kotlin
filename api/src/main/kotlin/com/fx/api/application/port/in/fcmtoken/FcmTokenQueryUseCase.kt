package com.fx.api.application.port.`in`.fcmtoken

import com.fx.common.domain.TopicType
import com.fx.common.domain.catalog.TopicView
import com.fx.common.domain.i18n.Language

interface FcmTokenQueryUseCase {

    /** 토큰이 구독한 [type] 토픽. code 오름차순. 삭제된 토픽은 뺀다. */
    fun getMyTopics(fcmToken: String, type: TopicType): List<TopicView>

    /** 토큰의 알림 언어. 저장된 값을 발송할 때와 같은 규칙으로 해석한다 (지원하지 않는 값은 한국어). */
    fun getLanguage(fcmToken: String): Language

}
