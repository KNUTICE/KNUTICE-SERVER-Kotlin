package com.fx.api.adapter.out.persistence.repository;

import com.fx.api.domain.NoticeQuery
import com.fx.global.adapter.out.persistence.document.NoticeDocument
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

/**
 * Non-blocking Repository
 * Mongo + QueryDSL 조합이 불가능하므로 ReactiveMongoTemplate 로 직접 구현
 *
 * @author 이동섭
 * @since 2026-03-15
 */
@Repository
class NoticeQueryRepository(
    private val reactiveMongoTemplate: ReactiveMongoTemplate
) {

    /**
     * Cursor 기반 조회
     * nttId 기준 DESC 정렬
     */
    suspend fun findByNotice(noticeQuery: NoticeQuery): List<NoticeDocument> {
        val query = Query().apply {
            eqNoticeType(noticeQuery)?.let { addCriteria(it) }
            containKeyword(noticeQuery.keyword)?.let { addCriteria(it) }
            ltNttId(noticeQuery.nttId)?.let { addCriteria(it) }

            with(Sort.by(Sort.Direction.DESC, "nttId"))
            limit(noticeQuery.pageable.pageSize)
        }

        return reactiveMongoTemplate.find(query, NoticeDocument::class.java)
            .collectList()
            .awaitSingle()
    }

    private fun eqNoticeType(noticeQuery: NoticeQuery): Criteria? {
        return noticeQuery.topic?.topicName?.let {
            Criteria.where("topic").`is`(it)
        }
    }

    private fun containKeyword(keyword: String?): Criteria? {
        return if (!keyword.isNullOrBlank()) {
            // "title" 필드에 keyword가 포함되어 있는지 확인 (i: 대소문자 무시)
            Criteria.where("title").regex(".*$keyword.*", "i")
        } else {
            null
        }
    }

    private fun ltNttId(nttId: Long?): Criteria? {
        return nttId?.let {
            Criteria.where("nttId").lt(it)
        }
    }

}