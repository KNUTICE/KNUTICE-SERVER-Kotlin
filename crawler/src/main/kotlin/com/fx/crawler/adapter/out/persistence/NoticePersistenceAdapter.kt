package com.fx.crawler.adapter.out.persistence

import com.fx.global.adapter.out.persistence.document.NoticeDocument
import com.fx.crawler.adapter.out.persistence.repository.NoticeMongoRepository
import com.fx.crawler.appllication.port.out.NoticePersistencePort
import com.fx.global.adapter.out.persistence.repository.NoticeQueryRepository
import com.fx.global.annotation.PersistenceAdapter
import com.fx.global.domain.Notice
import com.fx.global.domain.NoticeQuery

@PersistenceAdapter
class NoticePersistenceAdapter(
    private val noticeQueryRepository: NoticeQueryRepository,
    private val noticeMongoRepository: NoticeMongoRepository,
): NoticePersistencePort {

    override fun findByNttIdIn(nttIds: List<Long>): List<Notice> {
        return noticeMongoRepository.findByNttIdIn(nttIds)
            .map { it.toDomain() }
    }

    override fun saveAll(notices: List<Notice>) {
        val documents = notices.map { NoticeDocument.from(it) }
        noticeMongoRepository.saveAll(documents)
    }

    override fun getNotices(noticeQuery: NoticeQuery): List<Notice> =
        noticeQueryRepository.findByNotice(noticeQuery).map { it.toDomain() }

}