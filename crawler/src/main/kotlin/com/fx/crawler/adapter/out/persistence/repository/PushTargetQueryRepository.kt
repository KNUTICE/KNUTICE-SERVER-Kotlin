package com.fx.crawler.adapter.out.persistence.repository

import com.fx.common.domain.DeviceType
import com.fx.common.domain.fcmtoken.QFcmToken.Companion.fcmToken
import com.fx.common.domain.fcmtoken.QFcmTokenSubscription.Companion.fcmTokenSubscription
import com.fx.crawler.domain.push.PushTarget
import com.querydsl.core.Tuple
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

/** 발송 대상을 id keyset 으로 한 페이지씩 읽는다. `(id, token, language)` 만 읽는다. */
@Repository
class PushTargetQueryRepository(
    private val queryFactory: JPAQueryFactory,
) {

    /** `uk_fcm_token_subscription_topic_code_fcm_token_id (topic_code, fcm_token_id)` 범위 스캔. */
    fun findSubscribers(topicCode: Int, afterFcmTokenId: Long?, size: Int): List<PushTarget> =
        queryFactory
            .select(fcmTokenSubscription.fcmTokenId, fcmToken.token, fcmToken.language)
            .from(fcmTokenSubscription)
            .join(fcmToken).on(fcmToken.id.eq(fcmTokenSubscription.fcmTokenId))
            .where(
                fcmTokenSubscription.topicCode.eq(topicCode),
                afterFcmTokenId?.let { fcmTokenSubscription.fcmTokenId.gt(it) },
                fcmToken.isActive.isTrue,
            )
            .orderBy(fcmTokenSubscription.fcmTokenId.asc())
            .limit(size.toLong())
            .fetch()
            .map { it.toPushTarget(it.get(fcmTokenSubscription.fcmTokenId)) }

    fun findActiveIosTargets(afterFcmTokenId: Long?, size: Int): List<PushTarget> =
        queryFactory
            .select(fcmToken.id, fcmToken.token, fcmToken.language)
            .from(fcmToken)
            .where(
                afterFcmTokenId?.let { fcmToken.id.gt(it) },
                fcmToken.isActive.isTrue,
                fcmToken.deviceType.eq(DeviceType.iOS),
            )
            .orderBy(fcmToken.id.asc())
            .limit(size.toLong())
            .fetch()
            .map { it.toPushTarget(it.get(fcmToken.id)) }

    private fun Tuple.toPushTarget(fcmTokenId: Long?): PushTarget =
        PushTarget(
            fcmTokenId = requireNotNull(fcmTokenId),
            token = requireNotNull(get(fcmToken.token)),
            language = requireNotNull(get(fcmToken.language)),
        )

}
