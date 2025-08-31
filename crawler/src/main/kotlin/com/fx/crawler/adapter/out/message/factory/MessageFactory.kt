package com.fx.crawler.adapter.out.message.factory

import com.fx.crawler.domain.Notice
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

    private fun defaultData(notice: Notice): Map<String, String> =
        mapOf(
            "nttId" to notice.nttId.toString(),
            "contentUrl" to notice.contentUrl
        )

    private fun defaultNotification(notice: Notice, body: String): Notification =
        Notification.builder()
            .setTitle(notice.type.category)
            .setBody(body)
            .setImage(notice.contentImage)
            .build()

    private fun apnsConfig(notice: Notice): ApnsConfig =
        ApnsConfig.builder()
            .putHeader("apns-priority", "10")
            .setAps(
                Aps.builder()
                    .setMutableContent(true)
                    .setAlert(ApsAlert.builder().setLaunchImage(notice.contentImage).build())
                    .setSound("default")
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