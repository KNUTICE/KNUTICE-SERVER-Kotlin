package com.fx.crawler.adapter.out.message

import com.fx.crawler.adapter.out.message.factory.MessageFactory
import com.fx.crawler.appllication.port.out.FcmNotificationPort
import com.fx.crawler.common.annotation.NotificationAdapter
import com.fx.global.application.port.out.WebhookPort
import com.fx.global.domain.DeviceType
import com.fx.global.domain.FcmToken
import com.fx.global.domain.Meal
import com.fx.global.domain.Notice
import com.fx.global.domain.SlackMessage
import com.fx.global.domain.SlackType
import com.fx.readingroom.domain.SeatAlert
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MessagingErrorCode
import com.google.firebase.messaging.MulticastMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import kotlin.collections.plusAssign

@NotificationAdapter
class FcmNotificationAdapter(
    private val webhookPort: WebhookPort
) : FcmNotificationPort {

    private val log = LoggerFactory.getLogger(FcmNotificationAdapter::class.java)
    private val backgroundScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * @return failedTokens
     */
    override suspend fun sendNotification(fcmTokens: List<FcmToken>, notices: List<Notice>): List<FcmToken> {
        return withContext(Dispatchers.IO) {
            if (fcmTokens.isEmpty()) return@withContext emptyList()

            // 3개 이상인 경우 합쳐서
            // ex) notices = [n1, n2] -> noticeGroups = [[n1], [n2]]
            // ex) notices = [n1, n2, n3] -> noticeGroups = [[n1, n2, n3]]
            val noticeGroups = if (notices.size <= 2) notices.map { listOf(it) } else listOf(notices)
            val failedTokens = mutableListOf<FcmToken>()

            for (group in noticeGroups) {
                val first = group[0]

                // group.size == 1 -> 3개 이상인 경우임
                val bodyMessage = if (group.size == 1) first.title else buildMessage(group)

                failedTokens += sendWithRetry(fcmTokens) { tokens ->
                    MessageFactory.create(tokens, first, bodyMessage)
                }
            }

            failedTokens
        }
    }

    override suspend fun sendNotification(fcmTokens: List<FcmToken>, meal: Meal): List<FcmToken> {
        return withContext(Dispatchers.IO) {
            if (fcmTokens.isEmpty()) return@withContext emptyList()

            val bodyMessage = buildMessage(meal)

            sendWithRetry(fcmTokens) { tokens ->
                MessageFactory.create(tokens, meal, bodyMessage)
            }
        }
    }

    override suspend fun sendSilentPushNotification(fcmTokens: List<FcmToken>): List<FcmToken> {
        return withContext(Dispatchers.IO) {
            if (fcmTokens.isEmpty()) return@withContext emptyList()

            sendWithRetry(fcmTokens) { tokens ->
                MessageFactory.createSilentPush(tokens)
            }
        }
    }

    override suspend fun sendSeatAlert(alert: SeatAlert) {
        withContext(Dispatchers.IO) {
            val bodyMessage = "${alert.readingRoom.roomName} ${alert.seatNumber}번 좌석이 비었습니다!"
            // 임의로 더미 FcmToken 객체로 만들어서 전송
            sendWithRetry(listOf(FcmToken.createFcmToken(alert.fcmToken, DeviceType.UNKNOWN))) { tokens ->
                MessageFactory.create(tokens, alert, bodyMessage)
            }
        }
    }

    private suspend fun sendWithRetry(
        fcmTokens: List<FcmToken>,
        maxRetries: Int = 3,
        messageFactory: (List<String>) -> MulticastMessage
    ): List<FcmToken> {
        var attempt = 0
        var waitTime = 1L
        var currentTokens = fcmTokens
        val finalFailedTokens = mutableListOf<FcmToken>()

        while (attempt < maxRetries && currentTokens.isNotEmpty()) {
            attempt++
            try {
                val multicastMessage = messageFactory(currentTokens.map { it.fcmToken })
                val response = FirebaseMessaging.getInstance().sendEachForMulticast(multicastMessage)

                val retryableTokens = mutableListOf<FcmToken>()
                for ((index, sendResponse) in response.responses.withIndex()) {
                    if (!sendResponse.isSuccessful) {
                        when (sendResponse.exception?.messagingErrorCode) {
                            MessagingErrorCode.UNREGISTERED,
                            MessagingErrorCode.INVALID_ARGUMENT -> finalFailedTokens += currentTokens[index] // isActive = false 대상
                            MessagingErrorCode.INTERNAL,
                            MessagingErrorCode.UNAVAILABLE,
                            MessagingErrorCode.QUOTA_EXCEEDED -> retryableTokens += currentTokens[index] // 재시도 대상
                            else -> {
                                log.info("서버 오류로 전송할 수 없습니다. : ${sendResponse.exception?.messagingErrorCode}")
                                this.notifySlack("서버 오류로 전송할 수 없습니다. : ${sendResponse.exception?.messagingErrorCode}")
                            }
                        }
                    }
                }

                if (retryableTokens.isEmpty()) {
                    currentTokens = emptyList()
                    break
                }
                currentTokens = retryableTokens

                log.warn("일시적 오류 발생, ${waitTime}초 후 재시도 ($attempt/$maxRetries)")
                delay(waitTime * 1000)
                waitTime *= 2

            } catch (e: Exception) {
                log.error("FCM 전송 중 예외 발생 : ${e.message}", e)
                this.notifySlack("FCM 전송 중 예외 발생 : ${e.message}")
                break
            }

        }
        if (currentTokens.isNotEmpty()) {
            log.error("최종 실패 토큰 수(재시도 불가) : ${currentTokens.size}")
            this.notifySlack("최종 실패 토큰 수(재시도 불가) : ${currentTokens.size}")
        }

        return finalFailedTokens
    }

    private fun buildMessage(notices: List<Notice>): String {
        return "${notices[0].title} 외 ${notices.size - 1}개의 소식이 있습니다."
    }

    private fun buildMessage(meal: Meal): String {
        val header = "${meal.mealDate} ${meal.topic.category} 메뉴"
        val lines = mutableListOf<String>()

        meal.koreaMenus?.takeIf { it.isNotEmpty() }?.let {
            lines += "[한식]"
            lines += it
        }

        meal.topMenus?.takeIf { it.isNotEmpty() }?.let {
            if (lines.isNotEmpty()) lines += ""
            lines += "[일품]"
            lines += it
        }

        val menuText = if (lines.isEmpty()) {
            "등록된 식단 정보가 없습니다."
        } else {
            lines.joinToString("\n")
        }
        return "$header\n$menuText"
    }

    private fun notifySlack(content: String) {
        backgroundScope.launch {
            webhookPort.notifySlack(SlackMessage(
                content = content,
                type = SlackType.FCM_ERROR
            ))
        }
    }

}