package com.fx.crawler.adapter.out.message

import com.fx.crawler.adapter.out.message.factory.MessageFactory
import com.fx.crawler.appllication.port.out.FcmNotificationPort
import com.fx.crawler.common.annotation.NotificationAdapter
import com.fx.global.domain.FcmToken
import com.fx.global.domain.Notice
import com.google.firebase.messaging.BatchResponse
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MessagingErrorCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

@NotificationAdapter
class FcmNotificationAdapter : FcmNotificationPort {

    private val log = LoggerFactory.getLogger(FcmNotificationAdapter::class.java)

    /**
     * @return failedTokens
     */
    override suspend fun sendNotification(fcmTokens: List<FcmToken>, notices: List<Notice>): List<FcmToken> {
        return withContext(Dispatchers.IO) {
            if (fcmTokens.isEmpty()) return@withContext emptyList()

            val tokens = fcmTokens.map { it.fcmToken }
            val failedTokens = mutableListOf<FcmToken>()

            // 3개 이상인 경우 합쳐서
            // ex) notices = [n1, n2] -> noticeGroups = [[n1], [n2]]
            // ex) notices = [n1, n2, n3] -> noticeGroups = [[n1, n2, n3]]
            val noticeGroups = if (notices.size <= 2) notices.map { listOf(it) } else listOf(notices)

            for (group in noticeGroups) {
                val first = group[0]

                // group.size == 1 -> 3개 이상인 경우임
                val bodyMessage = if (group.size == 1) first.title else buildMessage(group)

                val multicastMessage = MessageFactory.create(tokens, first, bodyMessage)
                val response = FirebaseMessaging.getInstance().sendEachForMulticast(multicastMessage)
                failedTokens += extractFailedTokens(fcmTokens, response)
            }


            failedTokens
        }
    }

    private fun extractFailedTokens(fcmTokens: List<FcmToken>, response: BatchResponse): List<FcmToken> {
        return response.responses.mapIndexedNotNull { index, sendResponse ->
            if (!sendResponse.isSuccessful && (
                        sendResponse.exception?.messagingErrorCode == MessagingErrorCode.UNREGISTERED ||
                        sendResponse.exception?.messagingErrorCode == MessagingErrorCode.INVALID_ARGUMENT)
            ) fcmTokens[index] else null
        }
    }

    private fun buildMessage(notices: List<Notice>): String {
        return "${notices[0].title} 외 ${notices.size - 1}개의 소식이 있습니다."
    }
}