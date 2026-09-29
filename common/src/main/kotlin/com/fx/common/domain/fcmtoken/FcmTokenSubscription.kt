package com.fx.common.domain.fcmtoken

import com.fx.common.domain.catalog.TopicView
import com.fx.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

/**
 * 토큰의 토픽 구독 한 건. 구독은 INSERT, 해제는 DELETE 라 동시 요청에도 서로 덮어쓰지 않는다.
 * 토픽은 `topic_code` · `topic_name` 을 복제 저장한다.
 */
@Entity
@Table(
    name = "fcm_token_subscription",
    uniqueConstraints = [
        // 발송 대상 조회 : `WHERE topic_code = ? AND fcm_token_id > ? ORDER BY fcm_token_id` 의 커버링 인덱스를 겸한다
        UniqueConstraint(name = "uk_fcm_token_subscription_topic_code_fcm_token_id", columnNames = ["topic_code", "fcm_token_id"]),
    ],
    indexes = [
        // 내 구독 조회 : `WHERE fcm_token_id = ?`
        Index(name = "idx_fcm_token_subscription_fcm_token_id", columnList = "fcm_token_id"),
    ],
)
class FcmTokenSubscription(
    fcmTokenId: Long,
    topicCode: Int,
    topicName: String,
) : BaseEntity() {

    @Column(name = "fcm_token_id", nullable = false, updatable = false, comment = "FCM 토큰 ID")
    val fcmTokenId: Long = fcmTokenId

    @Column(name = "topic_code", nullable = false, updatable = false, comment = "토픽 코드 (v2 topicId)")
    val topicCode: Int = topicCode

    @Column(name = "topic_name", nullable = false, updatable = false, length = 100, comment = "토픽 이름 (v1 topic)")
    val topicName: String = topicName

    companion object {

        fun of(fcmTokenId: Long, topic: TopicView): FcmTokenSubscription =
            FcmTokenSubscription(fcmTokenId, topic.code, topic.name)

    }

}
