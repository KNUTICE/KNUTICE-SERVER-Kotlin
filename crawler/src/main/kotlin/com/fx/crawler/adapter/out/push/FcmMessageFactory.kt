package com.fx.crawler.adapter.out.push

import com.fx.crawler.domain.push.PushMessage
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.AndroidNotification
import com.google.firebase.messaging.ApnsConfig
import com.google.firebase.messaging.Aps
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification

/**
 * [PushMessage] 를 FCM multicast 메시지로 바꾼다. 앱은 `notification` 페이로드를 그대로 표시한다.
 * Firebase Admin 9.x 는 multicast 토큰 추가 메서드를 deprecated 로 표시했지만, 대체 API 없이 `sendEachForMulticast` 가 이 메시지를 받는다.
 */
@Suppress("DEPRECATION")
object FcmMessageFactory {

    fun notification(tokens: List<String>, message: PushMessage): MulticastMessage =
        MulticastMessage.builder()
            .putAllData(message.data)
            .setNotification(
                Notification.builder()
                    .setTitle(message.title)
                    .setBody(message.body)
                    .setImage(message.imageUrl)
                    .build()
            )
            .setApnsConfig(
                ApnsConfig.builder()
                    .putHeader("apns-priority", "10")
                    .setAps(Aps.builder().setMutableContent(true).setSound("default").build())
                    .build()
            )
            .setAndroidConfig(
                AndroidConfig.builder()
                    .setNotification(
                        AndroidNotification.builder()
                            .setSound("default")
                            .setPriority(AndroidNotification.Priority.HIGH)
                            .build()
                    )
                    .build()
            )
            .addAllTokens(tokens)
            .build()

    /** 표시하지 않는 iOS 백그라운드 푸시. 앱이 깨어나 토큰을 다시 등록한다. */
    fun silent(tokens: List<String>): MulticastMessage =
        MulticastMessage.builder()
            .setApnsConfig(
                ApnsConfig.builder()
                    .putHeader("apns-priority", "5")
                    .putHeader("apns-push-type", "background")
                    .putCustomData("event", "token_update")
                    .setAps(Aps.builder().setContentAvailable(true).setMutableContent(true).build())
                    .build()
            )
            .addAllTokens(tokens)
            .build()

}
