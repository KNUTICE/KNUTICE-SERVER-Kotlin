package com.fx.crawler.adapter.out.push

import com.fx.common.application.port.out.WebhookPort
import com.fx.common.domain.SlackMessage
import com.fx.common.domain.SlackType
import com.fx.crawler.application.port.out.PushPort
import com.fx.crawler.common.annotation.NotificationAdapter
import com.fx.crawler.config.CrawlerProperties
import com.fx.crawler.domain.push.PushMessage
import com.fx.crawler.domain.push.PushTarget
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MessagingErrorCode
import com.google.firebase.messaging.MulticastMessage
import org.slf4j.LoggerFactory
import java.time.Duration

/**
 * FCM 발송. 토큰 500개씩 `sendEachForMulticast` 로 보낸다.
 *
 * - 등록이 풀린 토큰(UNREGISTERED · INVALID_ARGUMENT)은 결과로 돌려주고, 호출한 쪽이 비활성화한다.
 * - 일시 오류(INTERNAL · UNAVAILABLE · QUOTA_EXCEEDED)는 해당 토큰만 1초 · 2초 간격으로 다시 보낸다.
 * - 발송은 되돌릴 수 없으므로 전송 예외를 밖으로 던지지 않는다. 던지면 chunk 가 다시 실행돼 같은 알림이 중복 발송될 수 있다.
 */
@NotificationAdapter
class FcmPushAdapter(
    private val firebaseMessaging: FirebaseMessaging,
    private val webhookPort: WebhookPort,
    private val properties: CrawlerProperties,
) : PushPort {

    private val log = LoggerFactory.getLogger(FcmPushAdapter::class.java)

    override fun send(targets: List<PushTarget>, message: PushMessage): List<Long> =
        targets.chunked(MAX_TOKENS_PER_MULTICAST).flatMap { batch ->
            sendWithRetry(batch) { tokens ->
                FcmMessageFactory.notification(tokens, message)
            }
        }

    override fun sendSilent(targets: List<PushTarget>): List<Long> =
        targets.chunked(MAX_TOKENS_PER_MULTICAST).flatMap { batch ->
            sendWithRetry(batch) { tokens ->
                FcmMessageFactory.silent(tokens)
            }
        }

    private fun sendWithRetry(targets: List<PushTarget>, messageOf: (List<String>) -> MulticastMessage): List<Long> {
        val invalidTokenIds = mutableListOf<Long>()
        val unexpectedErrors = mutableMapOf<String, Int>()
        var pending = targets
        var backoff = INITIAL_BACKOFF

        for (attempt in 1..properties.push.maxAttempts) {
            val response = try {
                firebaseMessaging.sendEachForMulticast(messageOf(pending.map {
                    it.token
                }))
            } catch (e: Exception) {
                log.error("FCM 전송 중 예외 발생 - 대상 {}건", pending.size, e)
                notifySlack("FCM 전송 중 예외 발생 (대상 ${pending.size}건) : ${e.message}")
                return invalidTokenIds
            }

            val retryable = mutableListOf<PushTarget>()
            response.responses.forEachIndexed { index, result ->
                if (result.isSuccessful) return@forEachIndexed
                when (val errorCode = result.exception?.messagingErrorCode) {
                    MessagingErrorCode.UNREGISTERED,
                    MessagingErrorCode.INVALID_ARGUMENT -> invalidTokenIds += pending[index].fcmTokenId
                    MessagingErrorCode.INTERNAL,
                    MessagingErrorCode.UNAVAILABLE,
                    MessagingErrorCode.QUOTA_EXCEEDED -> retryable += pending[index]
                    else -> unexpectedErrors.merge(errorCode?.name ?: "UNKNOWN", 1, Int::plus)
                }
            }

            pending = retryable
            if (pending.isEmpty() || attempt == properties.push.maxAttempts) break

            log.warn("FCM 일시 오류 {}건, {}ms 후 다시 보냄 ({}/{})", pending.size, backoff.toMillis(), attempt, properties.push.maxAttempts)
            try {
                Thread.sleep(backoff)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                break
            }
            backoff = backoff.multipliedBy(2)
        }

        if (unexpectedErrors.isNotEmpty()) {
            log.error("FCM 전송 실패 (재시도 불가) : {}", unexpectedErrors)
            notifySlack("FCM 전송 실패 (재시도 불가) : $unexpectedErrors")
        }
        if (pending.isNotEmpty()) {
            log.error("FCM 일시 오류가 계속돼 보내지 못한 토큰 : {}건", pending.size)
            notifySlack("FCM 일시 오류가 계속돼 보내지 못한 토큰 : ${pending.size}건")
        }
        return invalidTokenIds
    }

    private fun notifySlack(content: String) {
        webhookPort.notifySlack(SlackMessage.create(content, SlackType.FCM_ERROR))
    }

    companion object {
        /** FCM multicast 한 번에 보낼 수 있는 최대 토큰 수 */
        const val MAX_TOKENS_PER_MULTICAST = 500
        private val INITIAL_BACKOFF: Duration = Duration.ofSeconds(1)
    }

}
