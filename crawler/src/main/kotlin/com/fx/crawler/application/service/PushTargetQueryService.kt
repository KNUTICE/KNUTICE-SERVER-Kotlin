package com.fx.crawler.application.service

import com.fx.crawler.application.port.`in`.PushTargetQueryUseCase
import com.fx.crawler.application.port.out.FcmTokenPersistencePort
import com.fx.crawler.domain.push.PushTarget
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PushTargetQueryService(
    private val fcmTokenPersistencePort: FcmTokenPersistencePort,
) : PushTargetQueryUseCase {

    override fun findSubscribers(topicCode: Int, afterFcmTokenId: Long?, size: Int): List<PushTarget> =
        fcmTokenPersistencePort.findSubscribers(topicCode, afterFcmTokenId, size)

    override fun findActiveIosTargets(afterFcmTokenId: Long?, size: Int): List<PushTarget> =
        fcmTokenPersistencePort.findActiveIosTargets(afterFcmTokenId, size)

}
