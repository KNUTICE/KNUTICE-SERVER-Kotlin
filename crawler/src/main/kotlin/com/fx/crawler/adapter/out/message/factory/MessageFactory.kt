package com.fx.crawler.adapter.out.message.factory

import com.fx.global.domain.Meal
import com.fx.global.domain.Notice
import com.fx.readingroom.domain.SeatAlert
import com.google.firebase.messaging.*

object MessageFactory {

    fun create(
        tokens: List<String>,
        notice: Notice,
        bodyMessage: String
    ): MulticastMessage {
        return MulticastMessage.builder()
            .putAllData(deeplinkData(notice, bodyMessage))
            .setNotification(defaultNotification(notice, bodyMessage))
            .setApnsConfig(apnsConfig())
            .setAndroidConfig(androidConfig())
            .addAllTokens(tokens)
            .build()
    }

    fun create(
        tokens: List<String>,
        meal: Meal,
        bodyMessage: String
    ): MulticastMessage {
        return MulticastMessage.builder()
            .putAllData(deeplinkData(meal))
            .setNotification(mealNotification(meal, bodyMessage))
            .setApnsConfig(apnsConfig())
            .setAndroidConfig(androidConfig())
            .addAllTokens(tokens)
            .build()
    }

    fun createSilentPush(fcmTokens: List<String>): MulticastMessage =
        MulticastMessage.builder()
            .setApnsConfig(apnsSilentPushConfig())
            .addAllTokens(fcmTokens)
            .build()

    fun create(
        tokens: List<String>,
        alert: SeatAlert,
        bodyMessage: String
    ): MulticastMessage {
        return MulticastMessage.builder()
            .putAllData(deeplinkData(alert))
            .setNotification(seatAlertNotification(bodyMessage))
            .setApnsConfig(apnsConfig())
            .setAndroidConfig(androidConfig())
            .addAllTokens(tokens)
            .build()
    }

    private fun deeplinkData(notice: Notice, bodyMessage: String): Map<String, String> =
        mapOf(
            // 1.6.x 이상 호환 Deeplink
            "deeplink" to "knutice://notice?nttId=${notice.nttId}&contentUrl=${notice.contentUrl}&FabVisible=true",

            // 1.5.x 이하 호환 Deeplink
            // dslee (2026-02-21) : Android deeplink encoding 문제로 인해 nttId, contentUrl 따로 전달
            "nttId" to "${notice.nttId}",
            "contentUrl" to "${notice.contentUrl}",
        )

    private fun deeplinkData(meal: Meal): Map<String, String> =
        mapOf(
            "deeplink" to "knutice://meal?topic=${meal.topic.name}",

            "topic" to meal.topic.name
        )

    private fun deeplinkData(alert: SeatAlert): Map<String, String> =
        mapOf(
            "deeplink" to "knutice://reading-room?roomId=${alert.readingRoom.name}&seat=${alert.seatNumber}",
        )

    private fun defaultNotification(notice: Notice, body: String): Notification =
        Notification.builder()
            .setTitle(notice.topic.category)
            .setBody(body)
            .setImage(notice.contentImageUrl)
            .build()

    private fun mealNotification(meal: Meal, body: String): Notification =
        Notification.builder()
            .setTitle(meal.topic.category)
            .setBody(body)
            .build()

    private fun seatAlertNotification(body: String): Notification =
        Notification.builder()
            .setTitle("빈자리 알림")
            .setBody(body)
            .build()

    private fun apnsConfig(): ApnsConfig =
        ApnsConfig.builder()
            .putHeader("apns-priority", "10")
            .setAps(
                Aps.builder()
                    .setMutableContent(true)
                    .setSound("default")
                    .build()
            )
            .build()

    private fun apnsSilentPushConfig(): ApnsConfig =
        ApnsConfig.builder()
            .putHeader("apns-priority", "5")
            .putHeader("apns-push-type", "background")
            .putCustomData("event", "token_update")
            .setAps(Aps.builder()
                .setContentAvailable(true)
                .setMutableContent(true)
                .build()
            )
            .build()

    private fun androidConfig(): AndroidConfig =
        AndroidConfig.builder()
            .setNotification(
                AndroidNotification.builder()
                    .setSound("default")
                    .setPriority(AndroidNotification.Priority.HIGH)
                    .build()
            )
            .build()

}