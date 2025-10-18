package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.NoticeMongoRepository
import com.fx.api.adapter.out.persistence.repository.NoticeQueryRepository
import com.fx.api.application.port.out.NoticePersistencePort
import com.fx.api.domain.NoticeQuery
import com.fx.global.adapter.out.persistence.document.NoticeDocument
import com.fx.global.annotation.PersistenceAdapter
import com.fx.global.domain.Notice

@PersistenceAdapter
class NoticePersistenceAdapter(
    private val noticeQueryRepository: NoticeQueryRepository,
    private val noticePersistenceRepository: NoticeMongoRepository
): NoticePersistencePort {

    override fun getNotices(noticeQuery: NoticeQuery): List<Notice> =
        noticeQueryRepository.findByNotice(noticeQuery).map { it.toDomain() }

    override fun getNotice(nttId: Long): Notice? =
        noticePersistenceRepository.findById(nttId).orElse(null)?.toDomain()

    override fun saveNotice(notice: Notice) {
        noticePersistenceRepository.save(NoticeDocument.from(notice))
    }

}