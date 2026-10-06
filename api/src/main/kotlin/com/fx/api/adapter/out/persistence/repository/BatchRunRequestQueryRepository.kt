package com.fx.api.adapter.out.persistence.repository

import com.fx.common.domain.batch.BatchRunRequest
import com.fx.common.domain.batch.BatchRunRequestStatus
import com.fx.common.domain.batch.QBatchRunRequest.Companion.batchRunRequest
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository

/**
 * 수동 실행 요청 목록. 최신순(id 내림차순) 페이지 조회다.
 * 상태 필터가 있으면 `idx_batch_run_request_status_id`, 없으면 PK 를 탄다.
 */
@Repository
class BatchRunRequestQueryRepository(
    private val queryFactory: JPAQueryFactory,
) {

    fun findRequests(status: BatchRunRequestStatus?, pageable: Pageable): Page<BatchRunRequest> {
        val content = queryFactory
            .selectFrom(batchRunRequest)
            .where(statusEq(status))
            .orderBy(batchRunRequest.id.desc()) // TSID 는 생성 순서를 보장하므로 id 내림차순 = 최신순
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val countQuery = queryFactory
            .select(batchRunRequest.count())
            .from(batchRunRequest)
            .where(statusEq(status))

        // content 가 페이지 크기보다 작으면(마지막 페이지) count 쿼리를 생략한다.
        return PageableExecutionUtils.getPage(content, pageable) {
            countQuery.fetchOne() ?: 0L
        }
    }

    private fun statusEq(status: BatchRunRequestStatus?): BooleanExpression? =
        status?.let {
            batchRunRequest.status.eq(it)
        }

}
