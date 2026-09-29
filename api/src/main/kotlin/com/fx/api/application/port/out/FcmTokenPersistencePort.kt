package com.fx.api.application.port.out

import com.fx.common.domain.catalog.TopicView
import com.fx.common.domain.fcmtoken.FcmToken
import com.fx.common.exception.FcmTokenException

interface FcmTokenPersistencePort {

    fun findByToken(token: String): FcmToken?

    /** @throws FcmTokenException 토큰이 없을 때 (TOKEN_NOT_FOUND) */
    fun getByToken(token: String): FcmToken

    fun existsByToken(token: String): Boolean

    /** 토큰을 저장하고 [topics] 를 구독시킨다. */
    fun create(fcmToken: FcmToken, topics: List<TopicView>): FcmToken

    fun findSubscribedTopicCodes(fcmTokenId: Long): Set<Int>

    /** 구독한다. 이미 구독 중이면 아무 일도 하지 않는다. */
    fun subscribe(fcmTokenId: Long, topic: TopicView)

    /** 구독을 해제한다. 구독 중이 아니면 아무 일도 하지 않는다. */
    fun unsubscribe(fcmTokenId: Long, topicCode: Int)

    /** [toFcmTokenId] 의 구독을 [fromFcmTokenId] 의 구독과 같게 바꾼다. */
    fun copySubscriptions(fromFcmTokenId: Long, toFcmTokenId: Long)

}
