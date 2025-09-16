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
            .putAllData(defaultData(notice))
            .setNotification(defaultNotification(notice, bodyMessage))
            .setApnsConfig(apnsConfig(notice))
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

    private fun defaultData(notice: Notice): Map<String, String> =
        mapOf(
            "nttId" to notice.nttId.toString(),
            "contentUrl" to notice.contentUrl
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

    private fun apnsConfig(notice: Notice): ApnsConfig =
        ApnsConfig.builder()
            .putHeader("apns-priority", "10")
            .setAps(
                Aps.builder()
                    .setMutableContent(true)
                    .setAlert(ApsAlert.builder().setLaunchImage(notice.contentImageUrl).build())
                    .setSound("default")
                    .build()
            )
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