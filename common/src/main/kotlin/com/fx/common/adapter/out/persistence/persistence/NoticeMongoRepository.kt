package com.fx.common.adapter.out.persistence.repository;

import com.fx.common.adapter.out.persistence.document.NoticeDocument;
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.LocalDateTime;

interface NoticeMongoRepository : MongoRepository<NoticeDocument, Long> {

    fun findByNttIdIn(nttIds: List<Long>): List<NoticeDocument>

    fun countByCreatedAtLessThanEqual(dateTime: LocalDateTime): Long

    fun countByCreatedAtLessThanEqualAndContentSummaryIsNotNull(dateTime: LocalDateTime): Long

}