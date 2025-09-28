package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.NoticeMongoRepository
import com.fx.global.adapter.out.persistence.repository.NoticeQueryRepository
import com.fx.api.application.port.out.NoticePersistencePort
import com.fx.global.domain.NoticeQuery
import com.fx.global.annotation.PersistenceAdapter
import com.fx.global.domain.Notice

@PersistenceAdapter
class NoticePersistenceAdapter(
    private val noticeQueryRepository: NoticeQueryRepository,
    private val noticeMongoRepository: NoticeMongoRepository
): NoticePersistencePort {

    override fun getNotices(noticeQuery: NoticeQuery): List<Notice> =
        noticeQueryRepository.findByNotice(noticeQuery).map { it.toDomain() }

    override fun getNotice(nttId: Long): Notice? =
        noticeMongoRepository.findById(nttId).orElse(null)?.toDomain()
}