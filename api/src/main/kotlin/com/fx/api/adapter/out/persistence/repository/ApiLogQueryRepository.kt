package com.fx.api.adapter.out.persistence.repository

import com.fx.api.domain.ApiLogEntry
import com.fx.api.domain.ApiLogStatusGroup
import com.fx.api.domain.QApiLog.Companion.apiLog
import com.fx.common.domain.fcmtoken.QFcmToken.Companion.fcmToken
import com.querydsl.core.Tuple
import com.querydsl.core.types.Predicate
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import java.time.LocalDate

/**
 * 원본 API 로그 목록. 하루치를 요청 시각 · ID 내림차순으로 페이지 단위 조회한다.
 * `idx_api_log_requested_at` 은 PK(`id`)를 함께 담고 있어 이 순서대로 인덱스를 읽는다.
 */
@Repository
class ApiLogQueryRepository(
    private val queryFactory: JPAQueryFactory,
) {

    fun findApiLogs(date: LocalDate, urlPattern: String?, statusGroup: ApiLogStatusGroup?, pageable: Pageable): Page<ApiLogEntry> {
        val conditions = arrayOf<Predicate?>(
            apiLog.requestedAt.goe(date.atStartOfDay()),
            apiLog.requestedAt.lt(date.plusDays(1).atStartOfDay()),
            urlPatternEq(urlPattern),
            statusIn(statusGroup),
        )

        val content = queryFactory
            .select(
                apiLog.id,
                apiLog.requestedAt,
                apiLog.method,
                apiLog.urlPattern,
                apiLog.statusCode,
                apiLog.durationMs,
                apiLog.topicCode,
                apiLog.topicName,
                fcmToken.deviceType,
            )
            .from(apiLog)
            .leftJoin(fcmToken).on(fcmToken.id.eq(apiLog.fcmTokenId))
            .where(*conditions)
            .orderBy(apiLog.requestedAt.desc(), apiLog.id.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()
            .map {
                it.toEntry()
            }

        val countQuery = queryFactory
            .select(apiLog.count())
            .from(apiLog)
            .where(*conditions)

        // content 가 페이지 크기보다 작으면(마지막 페이지) count 쿼리를 생략한다.
        return PageableExecutionUtils.getPage(content, pageable) {
            countQuery.fetchOne() ?: 0L
        }
    }

    private fun Tuple.toEntry(): ApiLogEntry =
        ApiLogEntry(
            id = requireNotNull(get(apiLog.id)),
            requestedAt = requireNotNull(get(apiLog.requestedAt)),
            method = requireNotNull(get(apiLog.method)),
            urlPattern = requireNotNull(get(apiLog.urlPattern)),
            statusCode = requireNotNull(get(apiLog.statusCode)),
            durationMs = requireNotNull(get(apiLog.durationMs)),
            topicCode = get(apiLog.topicCode),
            topicName = get(apiLog.topicName),
            deviceType = get(fcmToken.deviceType),
        )

    private fun urlPatternEq(urlPattern: String?): BooleanExpression? =
        urlPattern?.takeIf {
            it.isNotBlank()
        }?.let {
            apiLog.urlPattern.eq(it)
        }

    private fun statusIn(statusGroup: ApiLogStatusGroup?): BooleanExpression? =
        statusGroup?.let {
            apiLog.statusCode.between(it.statusCodes.first, it.statusCodes.last)
        }

}
