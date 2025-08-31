package com.fx.crawler.adapter.out.persistence

import adapter.out.persistence.document.NoticeDocument
import com.fx.adapter.out.persistence.repository.NoticeMongoRepository
import com.fx.crawler.appllication.port.out.NoticePersistencePort
import com.fx.global.domain.Notice
import org.springframework.stereotype.Component

@Component
class NoticePersistenceAdapter(
    private val noticeMongoRepository: NoticeMongoRepository
): NoticePersistencePort {

    override fun findByNttIdIn(nttIds: List<Long>): List<Notice> {
        return noticeMongoRepository.findByNttIdIn(nttIds)
            .map { it.toDomain() }
    }

    override fun saveAll(notices: List<Notice>) {
        val documents = notices.map { NoticeDocument.from(it) }
        noticeMongoRepository.saveAll(documents)
    }

}