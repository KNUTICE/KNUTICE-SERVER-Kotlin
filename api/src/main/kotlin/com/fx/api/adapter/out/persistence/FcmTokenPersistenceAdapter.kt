package com.fx.api.adapter.out.persistence

import com.fx.global.adapter.out.persistence.repository.FcmTokenMongoRepository
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.api.application.port.out.dto.TopicUpdateQuery
import com.fx.global.adapter.out.persistence.document.FcmTokenDocument
import com.fx.global.annotation.PersistenceAdapter
import com.fx.global.domain.DeviceType
import com.fx.global.domain.FcmToken
import com.fx.global.domain.TopicType
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update

@PersistenceAdapter
class FcmTokenPersistenceAdapter(
    private val fcmTokenMongoRepository: FcmTokenMongoRepository,
    private val reactiveMongoTemplate: ReactiveMongoTemplate
): FcmTokenPersistencePort {

    override suspend fun saveFcmToken(fcmToken: FcmToken) {
        fcmTokenMongoRepository.save(FcmTokenDocument.from(fcmToken))
    }

    override suspend fun findByFcmToken(fcmToken: String): FcmToken? =
        fcmTokenMongoRepository.findById(fcmToken)?.toDomain()

    override suspend fun existsByFcmToken(fcmToken: String): Boolean =
        fcmTokenMongoRepository.existsById(fcmToken)

    override suspend fun countByIsActiveAndDeviceType(isActive: Boolean, deviceType: DeviceType): Long =
        fcmTokenMongoRepository.countByIsActiveAndDeviceType(isActive, deviceType)

    // 배타락으로 Lost Update 발생 방지
    override suspend fun atomicUpdateTopic(topicUpdateQuery: TopicUpdateQuery): Boolean {
        val field = when (topicUpdateQuery.topicType) {
            TopicType.NOTICE -> "subscribedNoticeTopics"
            TopicType.MAJOR  -> "subscribedMajorTopics"
            TopicType.MEAL   -> "subscribedMealTopics"
        }

        val mongoQuery = Query.query(Criteria.where("_id").`is`(topicUpdateQuery.fcmToken))
        val update = if (topicUpdateQuery.enabled) {
            Update().addToSet(field, topicUpdateQuery.topic.topicName)
        } else {
            Update().pull(field, topicUpdateQuery.topic.topicName)
        }

        return reactiveMongoTemplate
            .updateFirst(mongoQuery, update, FcmTokenDocument::class.java)
            .awaitSingle().matchedCount > 0
    }

}