package com.fx.global.adapter.out.persistence.document;

import com.fx.global.adapter.out.persistence.document.base.MongoBaseDocument
import com.fx.global.domain.MajorType;
import com.fx.global.domain.MealType;
import com.fx.global.domain.NoticeType;
import com.querydsl.core.annotations.QueryEntity;
import com.fx.global.domain.DeviceType;
import com.fx.global.domain.FcmToken;
import java.util.HashSet;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "fcm_token")
@QueryEntity
class FcmTokenDocument(

    @Id
    var fcmToken: String,
    var subscribedNoticeTopics: Set<NoticeType> = HashSet(),
    var subscribedMajorTopics: Set<MajorType> = HashSet(),
    var subscribedMealTopics: Set<MealType> = HashSet(),
    var deviceType: DeviceType = DeviceType.UNKNOWN,
    var isActive: Boolean = true

) : MongoBaseDocument() {

    fun toDomain(): FcmToken =
        FcmToken(
            fcmToken = this.fcmToken,
            subscribedNoticeTopics = this.subscribedNoticeTopics,
            subscribedMajorTopics = this.subscribedMajorTopics,
            subscribedMealTopics = this.subscribedMealTopics,
            deviceType = this.deviceType,
            isActive = this.isActive,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
    )

    companion object {
        fun from(fcmToken: FcmToken): FcmTokenDocument =
            FcmTokenDocument(
                fcmToken = fcmToken.fcmToken,
                subscribedNoticeTopics = fcmToken.subscribedNoticeTopics,
                subscribedMajorTopics = fcmToken.subscribedMajorTopics,
                subscribedMealTopics = fcmToken.subscribedMealTopics,
                deviceType = fcmToken.deviceType,
                isActive = fcmToken.isActive
            ).apply {
                this.createdAt = fcmToken.createdAt
                this.updatedAt = fcmToken.updatedAt
            }

        fun from(fcmTokens: List<FcmToken>): List<FcmTokenDocument> =
            fcmTokens.map { from(it) }

    }
}