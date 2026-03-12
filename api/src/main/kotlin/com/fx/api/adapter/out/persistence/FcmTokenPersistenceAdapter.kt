package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.FcmTokenMongoRepository
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.api.application.port.out.dto.TopicUpdateQuery
import com.fx.global.adapter.out.persistence.document.FcmTokenDocument
import com.fx.global.annotation.PersistenceAdapter
import com.fx.global.domain.DeviceType
import com.fx.global.domain.FcmToken
import com.fx.global.domain.TopicType
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update

@PersistenceAdapter
class FcmTokenPersistenceAdapter(
    private val fcmTokenMongoRepository: FcmTokenMongoRepository,
    private val mongoTemplate: MongoTemplate
): FcmTokenPersistencePort {

    override fun saveFcmToken(fcmToken: FcmToken) {
        fcmTokenMongoRepository.save(FcmTokenDocument.from(fcmToken))
    }

    override fun findByFcmToken(fcmToken: String): FcmToken? =
        fcmTokenMongoRepository.findById(fcmToken).orElse(null)?.toDomain()

    override fun existsByFcmToken(fcmToken: String): Boolean =
        fcmTokenMongoRepository.existsById(fcmToken)

    override fun countByIsActiveAndDeviceType(isActive: Boolean, deviceType: DeviceType): Long =
        fcmTokenMongoRepository.countByIsActiveAndDeviceType(isActive, deviceType)

    // 배타락으로 Lost Update 발생 방지
    override fun atomicUpdateTopic(topicUpdateQuery: TopicUpdateQuery): Boolean {
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

        return mongoTemplate.updateFirst(mongoQuery, update, FcmTokenDocument::class.java).matchedCount > 0
    }

}