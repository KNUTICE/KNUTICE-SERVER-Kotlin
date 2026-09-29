package com.fx.api.adapter.out.persistence

import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.common.adapter.out.persistence.repository.FcmTokenRepository
import com.fx.common.adapter.out.persistence.repository.FcmTokenSubscriptionRepository
import com.fx.common.annotation.PersistenceAdapter
import com.fx.common.domain.catalog.TopicView
import com.fx.common.domain.fcmtoken.FcmToken
import com.fx.common.domain.fcmtoken.FcmTokenSubscription
import com.fx.common.exception.FcmTokenException
import com.fx.common.exception.errorcode.FcmTokenErrorCode
import io.hypersistence.tsid.TSID
import java.time.Clock
import java.time.LocalDateTime

@PersistenceAdapter
class FcmTokenPersistenceAdapter(
    private val fcmTokenRepository: FcmTokenRepository,
    private val fcmTokenSubscriptionRepository: FcmTokenSubscriptionRepository,
    private val clock: Clock,
) : FcmTokenPersistencePort {

    override fun findByToken(token: String): FcmToken? =
        fcmTokenRepository.findByToken(token)

    override fun getByToken(token: String): FcmToken =
        fcmTokenRepository.findByToken(token)
            ?: throw FcmTokenException(FcmTokenErrorCode.TOKEN_NOT_FOUND)

    override fun existsByToken(token: String): Boolean =
        fcmTokenRepository.existsByToken(token)

    override fun create(fcmToken: FcmToken, topics: List<TopicView>): FcmToken {
        val saved = fcmTokenRepository.save(fcmToken)
        val fcmTokenId = requireNotNull(saved.id)
        fcmTokenSubscriptionRepository.saveAll(topics.map { FcmTokenSubscription.of(fcmTokenId, it) })
        return saved
    }

    override fun findSubscribedTopicCodes(fcmTokenId: Long): Set<Int> =
        fcmTokenSubscriptionRepository.findAllByFcmTokenId(fcmTokenId).mapTo(mutableSetOf()) { it.topicCode }

    override fun subscribe(fcmTokenId: Long, topic: TopicView) {
        fcmTokenSubscriptionRepository.insertIfAbsent(
            id = TSID.fast().toLong(),
            fcmTokenId = fcmTokenId,
            topicCode = topic.code,
            topicName = topic.name,
            now = LocalDateTime.now(clock),
        )
    }

    override fun unsubscribe(fcmTokenId: Long, topicCode: Int) {
        fcmTokenSubscriptionRepository.deleteByFcmTokenIdAndTopicCode(fcmTokenId, topicCode)
    }

    override fun copySubscriptions(fromFcmTokenId: Long, toFcmTokenId: Long) {
        val source = fcmTokenSubscriptionRepository.findAllByFcmTokenId(fromFcmTokenId)
        fcmTokenSubscriptionRepository.deleteAllByFcmTokenId(toFcmTokenId)
        fcmTokenSubscriptionRepository.saveAll(
            source.map { FcmTokenSubscription(toFcmTokenId, it.topicCode, it.topicName) }
        )
    }

}
