package com.fx.common.adapter.out.persistence.repository

import com.fx.common.domain.fcmtoken.FcmTokenSubscription
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface FcmTokenSubscriptionRepository : JpaRepository<FcmTokenSubscription, Long> {

    /**
     * 구독을 추가한다. 이미 있으면 아무것도 바꾸지 않는다.
     * 존재 확인 후 INSERT 하면 동시 요청이 유니크 제약에 걸려 트랜잭션이 롤백되므로 한 문장으로 처리한다.
     * TSID [id] 와 시각은 호출하는 쪽에서 채운다 (JPA 를 거치지 않으므로 생성기 · Auditing 이 동작하지 않는다).
     */
    @Modifying
    @Query(
        nativeQuery = true,
        value = """
            INSERT INTO fcm_token_subscription (id, fcm_token_id, topic_code, topic_name, created_at, updated_at)
            VALUES (:id, :fcmTokenId, :topicCode, :topicName, :now, :now)
            ON DUPLICATE KEY UPDATE id = id
        """,
    )
    fun insertIfAbsent(id: Long, fcmTokenId: Long, topicCode: Int, topicName: String, now: LocalDateTime): Int

    fun findAllByFcmTokenId(fcmTokenId: Long): List<FcmTokenSubscription>

    fun existsByFcmTokenIdAndTopicCode(fcmTokenId: Long, topicCode: Int): Boolean

    @Modifying
    @Query("DELETE FROM FcmTokenSubscription s WHERE s.fcmTokenId = :fcmTokenId AND s.topicCode = :topicCode")
    fun deleteByFcmTokenIdAndTopicCode(fcmTokenId: Long, topicCode: Int): Int

    @Modifying
    @Query("DELETE FROM FcmTokenSubscription s WHERE s.fcmTokenId = :fcmTokenId")
    fun deleteAllByFcmTokenId(fcmTokenId: Long): Int

}
