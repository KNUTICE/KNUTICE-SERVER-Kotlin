package com.fx.global.domain

import java.time.LocalDateTime

data class FcmToken(
    val fcmToken: String,
    val subscribedTopics: Set<String> = emptySet(),
    val deviceType: DeviceType,
    val isActive: Boolean,

    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {

    companion object {

        fun createFcmToken(fcmToken: String, deviceType: DeviceType): FcmToken {
            return FcmToken(
                fcmToken = fcmToken,
                subscribedTopics = CrawlableType.allTypeNames(),
                deviceType = deviceType,
                isActive = true,
                createdAt = LocalDateTime.now()
            )
        }

        fun updateFcmToken(newFcmToken: String, oldFcmToken: FcmToken): FcmToken {
            return FcmToken(
                fcmToken = newFcmToken,
                subscribedTopics = oldFcmToken.subscribedTopics,
                deviceType = oldFcmToken.deviceType,
                isActive = true,
                createdAt = oldFcmToken.createdAt
            )
        }

    }


}