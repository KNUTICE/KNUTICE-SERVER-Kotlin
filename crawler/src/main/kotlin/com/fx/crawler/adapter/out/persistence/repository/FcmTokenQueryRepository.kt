package com.fx.crawler.adapter.out.persistence.repository

import com.fx.crawler.domain.FcmTokenQuery
import com.fx.global.adapter.out.persistence.document.FcmTokenDocument
import com.fx.global.adapter.out.persistence.document.QFcmTokenDocument
import com.fx.global.domain.DeviceType
import com.fx.global.domain.MajorType
import com.fx.global.domain.MealType
import com.fx.global.domain.NoticeType
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.PathBuilder
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class FcmTokenQueryRepository(
    @Qualifier("mongoTemplate") operations: MongoOperations
) : QuerydslRepositorySupport(operations) {

    val fcmTokenDocument = QFcmTokenDocument.fcmTokenDocument

    /**
     * Cursor 기반 오름차순 batch 조회
     * createdAt 기준 정렬
     */
    fun findByCreatedAtAndIsActive(queryParam: FcmTokenQuery): List<FcmTokenDocument> =
        from(fcmTokenDocument)
            .where(
                cursorCondition(queryParam.createdAt, queryParam.pageable.sort),
                isActive(queryParam.isActive),
                eqSubscribedNoticeTopic(queryParam.subscribedNoticeTopic),
                eqSubscribedMajorTopic(queryParam.subscribedMajorTopic),
                eqSubscribedMealTopic(queryParam.subscribedMealTopic),
                eqDeviceType(queryParam.deviceType)
            )
            .orderBy(*getOrderSpecifiers(queryParam.pageable.sort))
            .limit(queryParam.pageable.pageSize.toLong())
            .fetch()

    private fun cursorCondition(createdAt: LocalDateTime?, sort: Sort): BooleanExpression? {
        createdAt ?: return null
        val order = sort.toList().firstOrNull() ?: Sort.Order.desc("createdAt")

        return if (order.isAscending) {
            fcmTokenDocument.createdAt.gt(createdAt)
        } else {
            fcmTokenDocument.createdAt.lt(createdAt)
        }
    }

    private fun isActive(isActive: Boolean): BooleanExpression =
        fcmTokenDocument.isActive.eq(isActive)

    private fun eqSubscribedNoticeTopic(topic: NoticeType?): BooleanExpression? =
        topic?.let { fcmTokenDocument.subscribedNoticeTopics.contains(it) }

    private fun eqSubscribedMajorTopic(topic: MajorType?): BooleanExpression? =
        topic?.let { fcmTokenDocument.subscribedMajorTopics.contains(it) }

    private fun eqSubscribedMealTopic(topic: MealType?): BooleanExpression? =
        topic?.let { fcmTokenDocument.subscribedMealTopics.contains(it) }

    private fun eqDeviceType(deviceType: DeviceType?): BooleanExpression? =
        deviceType?.let { fcmTokenDocument.deviceType.eq(it) }

    private fun getOrderSpecifiers(sort: Sort): Array<OrderSpecifier<*>> =
        sort.toList().map { order ->
            val pathBuilder = PathBuilder(FcmTokenDocument::class.java, "fcmTokenDocument")
            val direction = if (order.isDescending) Order.DESC else Order.ASC

            @Suppress("UNCHECKED_CAST")
            OrderSpecifier(
                direction,
                pathBuilder.get(order.property) as com.querydsl.core.types.Expression<out Comparable<*>>
            )
        }.toTypedArray()

}