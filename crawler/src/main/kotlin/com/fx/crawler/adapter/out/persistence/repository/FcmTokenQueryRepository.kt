package com.fx.crawler.adapter.out.persistence.repository

import com.fx.crawler.domain.FcmTokenQuery
import com.fx.global.adapter.out.persistence.document.FcmTokenDocument
import com.fx.global.domain.DeviceType
import com.fx.global.domain.MajorType
import com.fx.global.domain.MealType
import com.fx.global.domain.NoticeType
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class FcmTokenQueryRepository(
    private val reactiveMongoTemplate: ReactiveMongoTemplate
) {

    /**
     * Cursor 기반 오름차순/내림차순 batch 조회
     * createdAt 기준 정렬
     */
    suspend fun findByCreatedAtAndIsActive(queryParam: FcmTokenQuery): List<FcmTokenDocument> {
        val query = Query().apply {
            cursorCondition(queryParam.createdAt, queryParam.pageable.sort)?.let { addCriteria(it) }

            addCriteria(isActive(queryParam.isActive))

            eqSubscribedNoticeTopic(queryParam.subscribedNoticeTopic)?.let { addCriteria(it) }
            eqSubscribedMajorTopic(queryParam.subscribedMajorTopic)?.let { addCriteria(it) }
            eqSubscribedMealTopic(queryParam.subscribedMealTopic)?.let { addCriteria(it) }
            eqDeviceType(queryParam.deviceType)?.let { addCriteria(it) }

            with(queryParam.pageable.sort)
            limit(queryParam.pageable.pageSize)
        }

        return reactiveMongoTemplate.find(query, FcmTokenDocument::class.java)
            .collectList()
            .awaitSingle()
    }

    private fun cursorCondition(createdAt: LocalDateTime?, sort: Sort): Criteria? {
        if (createdAt == null) return null

        val order = sort.firstOrNull() ?: Sort.Order.desc("createdAt")

        return if (order.isAscending) {
            Criteria.where("createdAt").gt(createdAt)
        } else {
            Criteria.where("createdAt").lt(createdAt)
        }
    }

    private fun isActive(isActive: Boolean): Criteria {
        return Criteria.where("isActive").`is`(isActive)
    }

    private fun eqSubscribedNoticeTopic(topic: NoticeType?): Criteria? {
        return topic?.let { Criteria.where("subscribedNoticeTopics").`is`(it) }
    }

    private fun eqSubscribedMajorTopic(topic: MajorType?): Criteria? {
        return topic?.let { Criteria.where("subscribedMajorTopics").`is`(it) }
    }

    private fun eqSubscribedMealTopic(topic: MealType?): Criteria? {
        return topic?.let { Criteria.where("subscribedMealTopics").`is`(it) }
    }

    private fun eqDeviceType(deviceType: DeviceType?): Criteria? {
        return deviceType?.let { Criteria.where("deviceType").`is`(it) }
    }
    
}