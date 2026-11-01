package com.fx.api.adapter.out.persistence.repository

import com.fx.common.domain.topic.QTopic.Companion.topic
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

/** 단과대 목록에 붙일 소속 학과 수. 단과대마다 따로 세지 않고 한 번의 GROUP BY 로 센다. */
@Repository
class CollegeQueryRepository(
    private val queryFactory: JPAQueryFactory,
) {

    fun countTopicsByCollegeId(): Map<Long, Long> {
        val topicCount = topic.count()
        return queryFactory
            .select(topic.collegeId, topicCount)
            .from(topic)
            .where(topic.collegeId.isNotNull, topic.deletedAt.isNull)
            .groupBy(topic.collegeId)
            .fetch()
            .associate {
                requireNotNull(it.get(topic.collegeId)) to (it.get(topicCount) ?: 0L)
            }
    }

}
