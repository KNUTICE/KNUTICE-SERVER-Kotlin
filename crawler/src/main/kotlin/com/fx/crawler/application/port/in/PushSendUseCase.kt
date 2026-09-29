package com.fx.crawler.application.port.`in`

import com.fx.crawler.domain.push.LocalizedPushMessages
import com.fx.crawler.domain.push.PushSendResult
import com.fx.crawler.domain.push.PushTarget

interface PushSendUseCase {

    /** 토큰을 알림 언어별로 묶어 그 언어의 알림을 보내고, 등록이 풀린 토큰은 비활성화한다. */
    fun send(targets: List<PushTarget>, messages: LocalizedPushMessages): PushSendResult

    fun sendSilent(targets: List<PushTarget>): PushSendResult

}
