package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.NoticeQueryRepository
import com.fx.api.application.port.out.NoticePersistencePort
import com.fx.api.domain.NoticeQuery
import com.fx.global.annotation.PersistenceAdapter
import com.fx.global.domain.Notice

@PersistenceAdapter
class NoticePersistenceAdapter(
    private val noticeQueryRepository: NoticeQueryRepository
): NoticePersistencePort {

    override fun getNotices(noticeQuery: NoticeQuery): List<Notice> =
        noticeQueryRepository.findByNotice(noticeQuery).map { it.toDomain() }

}