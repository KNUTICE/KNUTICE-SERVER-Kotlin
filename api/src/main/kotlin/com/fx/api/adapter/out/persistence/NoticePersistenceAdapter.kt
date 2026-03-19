package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.NoticeQueryRepository
import com.fx.global.adapter.out.persistence.repository.NoticeMongoRepository
import com.fx.api.application.port.out.NoticePersistencePort
import com.fx.api.domain.NoticeQuery
import com.fx.global.adapter.out.persistence.document.NoticeDocument
import com.fx.global.annotation.PersistenceAdapter
import com.fx.global.domain.Notice
import java.time.LocalDateTime

@PersistenceAdapter
class NoticePersistenceAdapter(
    private val noticeQueryRepository: NoticeQueryRepository,
    private val noticeMongoRepository: NoticeMongoRepository
): NoticePersistencePort {

    override suspend fun getNotices(noticeQuery: NoticeQuery): List<Notice> =
        noticeQueryRepository.findByNotice(noticeQuery).map { it.toDomain() }

    override suspend fun getNotice(nttId: Long): Notice? =
        noticeMongoRepository.findById(nttId)?.toDomain()

    override suspend fun saveNotice(notice: Notice) {
        noticeMongoRepository.save(NoticeDocument.from(notice))
    }

    override suspend fun deleteById(nttId: Long) {
        noticeMongoRepository.deleteById(nttId)
    }

    override suspend fun existsById(nttId: Long): Boolean =
        noticeMongoRepository.existsById(nttId)

    override suspend fun countByCreatedAtLessThanEqual(dateTime: LocalDateTime): Long =
        noticeMongoRepository.countByCreatedAtLessThanEqual(dateTime)

    override suspend fun countByCreatedAtLessThanEqualAndHasSummary(dateTime: LocalDateTime): Long =
        noticeMongoRepository.countByCreatedAtLessThanEqualAndContentSummaryIsNotNull(dateTime)

}