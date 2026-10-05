package com.fx.crawler.application.port.out

import com.fx.common.exception.FcmTokenException
import com.fx.crawler.domain.push.PushTarget
import java.time.LocalDateTime

interface FcmTokenPersistencePort {

    /** 토픽을 구독한 활성 토큰을 토큰 id 순으로 [afterFcmTokenId] 다음부터 [size] 개. */
    fun findSubscribers(topicCode: Int, afterFcmTokenId: Long?, size: Int): List<PushTarget>

    /** 활성 iOS 토큰을 id 순으로 [afterFcmTokenId] 다음부터 [size] 개. */
    fun findActiveIosTargets(afterFcmTokenId: Long?, size: Int): List<PushTarget>

    fun deactivateAll(fcmTokenIds: Collection<Long>, now: LocalDateTime): Int

    /** @throws FcmTokenException 토큰이 없을 때 */
    fun getTargetByToken(token: String): PushTarget

}
