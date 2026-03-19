package com.fx.crawler.adapter.out.persistence

import com.fx.global.adapter.out.persistence.document.NoticeDocument
import com.fx.crawler.appllication.port.out.NoticePersistencePort
import com.fx.global.adapter.out.persistence.repository.NoticeMongoRepository
import com.fx.global.annotation.PersistenceAdapter
import com.fx.global.domain.Notice
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.time.LocalDateTime

@PersistenceAdapter
class NoticePersistenceAdapter(
    private val noticeMongoRepository: NoticeMongoRepository
): NoticePersistencePort {

    override suspend fun findByNttIdIn(nttIds: List<Long>): List<Notice> {
        return noticeMongoRepository.findByNttIdIn(nttIds)
            .map { it.toDomain() }
            .toList()
    }

    override suspend fun saveAll(notices: List<Notice>) {
        val documents = notices.map { NoticeDocument.from(it) }
        noticeMongoRepository.saveAll(documents).collect()
    }

    override suspend fun countByCreatedAtLessThanEqual(dateTime: LocalDateTime): Long =
        noticeMongoRepository.countByCreatedAtLessThanEqual(dateTime)

    override suspend fun countByCreatedAtLessThanEqualAndHasSummary(dateTime: LocalDateTime): Long =
        noticeMongoRepository.countByCreatedAtLessThanEqualAndContentSummaryIsNotNull(dateTime)

}