package com.fx.crawler.adapter.out.message

import com.fx.crawler.appllication.port.out.FcmNotificationPort
import com.fx.crawler.common.annotation.NotificationAdapter
import com.fx.crawler.domain.CrawlableType
import com.fx.crawler.domain.FcmToken
import com.google.firebase.messaging.BatchResponse
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MessagingErrorCode
import com.google.firebase.messaging.MulticastMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@NotificationAdapter
class FcmNotificationAdapter : FcmNotificationPort {

    /**
     * @return failedTokens
     */
    override suspend fun sendNotification(fcmTokens: List<FcmToken>, notice: CrawlableType, message: String): List<FcmToken> {
        return withContext(Dispatchers.IO) {
            if (fcmTokens.isEmpty()) return@withContext emptyList()

            val tokens = fcmTokens.map { it.fcmToken }

            val message: MulticastMessage = MulticastMessage.builder() // TODO Factory 처리
                .putData("title", "${notice.category}")
                .putData("message", "$message")
                .addAllTokens(tokens)
                .build()

            val response: BatchResponse = FirebaseMessaging.getInstance().sendEachForMulticast(message)

            // UNREGISTERED 인 경우 실패로 간주합니다.
            val failedTokens = response.responses.mapIndexedNotNull { index, sendResponse ->
                if (!sendResponse.isSuccessful && sendResponse.exception?.messagingErrorCode == MessagingErrorCode.UNREGISTERED) {
                    fcmTokens[index]
                } else null
            }

            failedTokens // 실패 토큰 반환
        }
    }


}