package com.fx.crawler.application.port.`in`

import com.fx.crawler.domain.push.PushTarget

/** 발송 Step 이 페이지 단위로 읽는 발송 대상. */
interface PushTargetQueryUseCase {

    fun findSubscribers(topicCode: Int, afterFcmTokenId: Long?, size: Int): List<PushTarget>

    fun findActiveIosTargets(afterFcmTokenId: Long?, size: Int): List<PushTarget>

}
