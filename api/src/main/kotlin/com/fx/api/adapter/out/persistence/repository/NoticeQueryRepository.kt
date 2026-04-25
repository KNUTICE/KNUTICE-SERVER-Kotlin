package com.fx.api.adapter.out.persistence.repository

import com.fx.api.domain.NoticeQuery
import com.fx.global.adapter.out.persistence.document.NoticeDocument
import com.fx.global.adapter.out.persistence.document.QNoticeDocument
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.Predicate
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.PathBuilder
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class NoticeQueryRepository(
    @Qualifier("mongoTemplate") operations: MongoOperations
) : QuerydslRepositorySupport(operations) {

    val noticeDocument = QNoticeDocument.noticeDocument

    /**
     * Cursor 기반 조회
     * nttId 기준 DESC 정렬
     */
    fun findByNotice(noticeQuery: NoticeQuery): List<NoticeDocument> =
        from(noticeDocument)
            .where(
                eqNoticeType(noticeQuery),
                containKeyword(noticeQuery.keyword),
                ltNttId(noticeQuery.nttId)
            )
            .orderBy(*getOrderSpecifiers(noticeQuery.pageable.sort))
            .limit(noticeQuery.pageable.pageSize.toLong())
            .fetch()

    private fun eqNoticeType(noticeQuery: NoticeQuery): BooleanExpression? =
        noticeQuery.topic?.let { noticeDocument.topic.eq(it.topicName) }

    private fun containKeyword(keyword: String?): BooleanExpression? =
        // keyword가 null이 아니고, 비어있지 않을 때
        keyword?.takeIf { it.isNotBlank() }?.let { noticeDocument.title.contains(it) }

    private fun ltNttId(nttId: Long?): Predicate? =
        nttId?.let { noticeDocument.nttId.lt(it) }

    private fun getOrderSpecifiers(sort: Sort): Array<OrderSpecifier<*>> =
        sort.toList()
            .map { order ->
                val pathBuilder = PathBuilder(NoticeDocument::class.java, "noticeDocument")
                val direction = if (order.isDescending) Order.DESC else Order.ASC

                @Suppress("UNCHECKED_CAST")
                OrderSpecifier(
                    direction,
                    pathBuilder.get(order.property) as com.querydsl.core.types.Expression<out Comparable<*>>
                )
            }.toTypedArray()

}