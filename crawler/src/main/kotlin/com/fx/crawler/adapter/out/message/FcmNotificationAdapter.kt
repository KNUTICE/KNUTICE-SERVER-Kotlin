package com.fx.crawler.adapter.out.message

import com.fx.crawler.appllication.port.out.FcmNotificationPort
import com.fx.crawler.common.annotation.NotificationAdapter
import com.fx.crawler.domain.FcmToken
import com.fx.crawler.domain.Notice
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.AndroidNotification
import com.google.firebase.messaging.AndroidNotification.Priority
import com.google.firebase.messaging.ApnsConfig
import com.google.firebase.messaging.Aps
import com.google.firebase.messaging.ApsAlert
import com.google.firebase.messaging.BatchResponse
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MessagingErrorCode
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
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
                val messageBody = if (group.size == 1) first.title else buildMessage(group)

                val multicastMessage = buildMulticastMessage(tokens, first, messageBody)
                val response = FirebaseMessaging.getInstance().sendEachForMulticast(multicastMessage)
                failedTokens += extractFailedTokens(fcmTokens, response)
            }


            failedTokens
        }
    }

    private fun buildMulticastMessage(tokens: List<String>, notice: Notice, body: String): MulticastMessage {
        return MulticastMessage.builder()
            .putAllData(mapOf(
                "nttId" to notice.nttId.toString(),
                "contentUrl" to notice.contentUrl
            ))
            .setNotification(Notification.builder()
                .setTitle(notice.type.category)
                .setBody(body)
                .setImage(notice.contentImage)
                .build())
            .setApnsConfig(ApnsConfig.builder()
                .putHeader("apns-priority", "10")
                .setAps(Aps.builder()
                    .setMutableContent(true)
                    .setAlert(ApsAlert.builder().setLaunchImage(notice.contentImage).build())
                    .setSound("default")
                    .build())
                .build())
            .setAndroidConfig(AndroidConfig.builder()
                .setNotification(AndroidNotification.builder()
                    .setSound("default")
                    .setPriority(Priority.HIGH)
                    .build())
                .build())
            .addAllTokens(tokens)
            .build()
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