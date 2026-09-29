package com.fx.crawler.application.service

import com.fx.crawler.application.port.`in`.PushSendUseCase
import com.fx.crawler.application.port.out.FcmTokenPersistencePort
import com.fx.crawler.application.port.out.PushPort
import com.fx.crawler.domain.push.LocalizedPushMessages
import com.fx.crawler.domain.push.PushSendResult
import com.fx.crawler.domain.push.PushTarget
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDateTime

@Service
class PushSendService(
    private val pushPort: PushPort,
    private val fcmTokenPersistencePort: FcmTokenPersistencePort,
    private val clock: Clock,
) : PushSendUseCase {

    override fun send(targets: List<PushTarget>, messages: LocalizedPushMessages): PushSendResult {
        val invalidTokenIds = mutableSetOf<Long>()
        targets.groupBy { it.resolveLanguage() }.forEach { (language, sameLanguageTargets) ->
            messages.of(language).forEach { message ->
                invalidTokenIds += pushPort.send(sameLanguageTargets, message)
            }
        }
        return complete(targets, invalidTokenIds)
    }

    override fun sendSilent(targets: List<PushTarget>): PushSendResult =
        complete(targets, pushPort.sendSilent(targets).toSet())

    private fun complete(targets: List<PushTarget>, invalidTokenIds: Set<Long>): PushSendResult {
        if (invalidTokenIds.isNotEmpty()) {
            fcmTokenPersistencePort.deactivateAll(invalidTokenIds, LocalDateTime.now(clock))
        }
        return PushSendResult(targetCount = targets.size, deactivatedTokenIds = invalidTokenIds)
    }

}
