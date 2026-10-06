package com.fx.api.adapter.out.persistence.repository

import com.fx.common.domain.batch.BatchRunRequest
import com.fx.common.domain.batch.BatchRunRequestStatus
import com.fx.common.domain.batch.QBatchRunRequest.Companion.batchRunRequest
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

/**
 * 수동 실행 요청 목록. id 를 커서로 쓰는 keyset 방식이며 최신순(id 내림차순)이다.
 * 상태 필터가 있으면 `idx_batch_run_request_status_id`, 없으면 PK 를 탄다.
 */
@Repository
class BatchRunRequestQueryRepository(
    private val queryFactory: JPAQueryFactory,
) {

    fun findRequests(status: BatchRunRequestStatus?, cursor: Long?, limit: Int): List<BatchRunRequest> =
        queryFactory
            .selectFrom(batchRunRequest)
            .where(
                statusEq(status),
                idLt(cursor),
            )
            .orderBy(batchRunRequest.id.desc())
            .limit(limit.toLong())
            .fetch()

    private fun statusEq(status: BatchRunRequestStatus?): BooleanExpression? =
        status?.let {
            batchRunRequest.status.eq(it)
        }

    private fun idLt(cursor: Long?): BooleanExpression? =
        cursor?.let {
            batchRunRequest.id.lt(it)
        }

}
