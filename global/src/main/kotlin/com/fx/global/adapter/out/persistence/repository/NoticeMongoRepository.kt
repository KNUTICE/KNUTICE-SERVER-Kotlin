package com.fx.global.adapter.out.persistence.repository;

import com.fx.global.adapter.out.persistence.document.NoticeDocument;
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.LocalDateTime;

interface NoticeMongoRepository : CoroutineCrudRepository<NoticeDocument, Long> {

    fun findByNttIdIn(nttIds: Collection<Long>): Flow<NoticeDocument>

    // 카운트 쿼리는 suspend 함수로 선언
    suspend fun countByCreatedAtLessThanEqual(dateTime: LocalDateTime): Long

    suspend fun countByCreatedAtLessThanEqualAndContentSummaryIsNotNull(dateTime: LocalDateTime): Long

}