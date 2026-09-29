package com.fx.crawler.adapter.out.persistence

import com.fx.common.adapter.out.persistence.repository.FcmTokenRepository
import com.fx.common.annotation.PersistenceAdapter
import com.fx.common.exception.FcmTokenException
import com.fx.common.exception.errorcode.FcmTokenErrorCode
import com.fx.crawler.adapter.out.persistence.repository.PushTargetQueryRepository
import com.fx.crawler.application.port.out.FcmTokenPersistencePort
import com.fx.crawler.domain.push.PushTarget
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@PersistenceAdapter
class FcmTokenPersistenceAdapter(
    private val fcmTokenRepository: FcmTokenRepository,
    private val pushTargetQueryRepository: PushTargetQueryRepository,
) : FcmTokenPersistencePort {

    @Transactional(readOnly = true)
    override fun findSubscribers(topicCode: Int, afterFcmTokenId: Long?, size: Int): List<PushTarget> =
        pushTargetQueryRepository.findSubscribers(topicCode, afterFcmTokenId, size)

    @Transactional(readOnly = true)
    override fun findActiveIosTargets(afterFcmTokenId: Long?, size: Int): List<PushTarget> =
        pushTargetQueryRepository.findActiveIosTargets(afterFcmTokenId, size)

    /** 벌크 UPDATE 한 번으로 비활성화한다. 발송 중인 다른 chunk 가 읽은 토큰 상태를 덮어쓰지 않는다. */
    @Transactional
    override fun deactivateAll(fcmTokenIds: Collection<Long>, now: LocalDateTime): Int =
        if (fcmTokenIds.isEmpty()) 0 else fcmTokenRepository.deactivateAll(fcmTokenIds, now)

    @Transactional(readOnly = true)
    override fun getTargetByToken(token: String): PushTarget {
        val fcmToken = fcmTokenRepository.findByToken(token)
            ?: throw FcmTokenException(FcmTokenErrorCode.TOKEN_NOT_FOUND)
        return PushTarget(requireNotNull(fcmToken.id), fcmToken.token, fcmToken.language)
    }

}
