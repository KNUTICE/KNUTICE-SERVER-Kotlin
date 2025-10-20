package com.fx.global.domain

import java.time.LocalDateTime

data class FcmToken(
    val fcmToken: String,
    val subscribedNoticeTopics: Set<NoticeType> = emptySet(),
    val subscribedMajorTopics: Set<MajorType> = emptySet(),
    val subscribedMealTopics: Set<MealType> = emptySet(),
    val deviceType: DeviceType,
    val isActive: Boolean,

    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {

    companion object {

        fun createFcmToken(fcmToken: String, deviceType: DeviceType): FcmToken {
            return FcmToken(
                fcmToken = fcmToken,
                subscribedNoticeTopics = NoticeType.entries.toSet(),
//                subscribedMealTopics = MealType.entries.toSet(), // TODO 나중에 활성화!!
                deviceType = deviceType,
                isActive = true,
                createdAt = LocalDateTime.now()
            )
        }

        fun updateFcmToken(newFcmToken: String, oldFcmToken: FcmToken): FcmToken {
            return FcmToken(
                fcmToken = newFcmToken,
                subscribedNoticeTopics = oldFcmToken.subscribedNoticeTopics,
                subscribedMajorTopics =  oldFcmToken.subscribedMajorTopics,
                subscribedMealTopics = oldFcmToken.subscribedMealTopics,
                deviceType = oldFcmToken.deviceType,
                isActive = true,
                createdAt = oldFcmToken.createdAt
            )
        }

    }


}