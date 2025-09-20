package com.fx.crawler.adapter.out.message.factory

import com.fx.global.domain.Meal
import com.fx.global.domain.Notice
import com.google.firebase.messaging.*

object MessageFactory {

    fun create(
        tokens: List<String>,
        notice: Notice,
        bodyMessage: String
    ): MulticastMessage {
        return MulticastMessage.builder()
            .putAllData(deeplinkData(notice))
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

    private fun deeplinkData(notice: Notice): Map<String, String> =
        mapOf(
            "deeplink" to "knutice://notice?nttId=${notice.nttId}&contentUrl=${notice.contentUrl}&FabVisible=true"
        )

    private fun deeplinkData(meal: Meal): Map<String, String> =
        mapOf(
            "deeplink" to "knutice://meal"
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