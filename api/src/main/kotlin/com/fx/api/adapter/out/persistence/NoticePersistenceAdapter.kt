package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.NoticeQueryRepository
import com.fx.api.application.port.out.notice.NoticePersistencePort
import com.fx.api.domain.NoticeQuery
import com.fx.common.adapter.out.persistence.repository.NoticeContentRepository
import com.fx.common.adapter.out.persistence.repository.NoticeRepository
import com.fx.common.annotation.PersistenceAdapter
import com.fx.common.domain.notice.Notice
import com.fx.common.domain.notice.NoticeContent
import com.fx.common.exception.NoticeException
import com.fx.common.exception.errorcode.NoticeErrorCode

@PersistenceAdapter
class NoticePersistenceAdapter(
    private val noticeRepository: NoticeRepository,
    private val noticeContentRepository: NoticeContentRepository,
    private val noticeQueryRepository: NoticeQueryRepository,
) : NoticePersistencePort {

    override fun findNotices(noticeQuery: NoticeQuery): List<Notice> =
        noticeQueryRepository.findNotices(noticeQuery)

    override fun getNotice(nttId: Long): Notice =
        noticeRepository.findByNttId(nttId)
            ?: throw NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND)

    override fun existsByNttId(nttId: Long): Boolean =
        noticeRepository.existsByNttId(nttId)

    override fun create(notice: Notice, contentSummary: String?): Notice {
        val saved = noticeRepository.save(notice)
        noticeContentRepository.save(
            NoticeContent(noticeId = requireNotNull(saved.id), content = null, contentSummary = contentSummary?.takeIf { it.isNotBlank() })
        )
        return saved
    }

    override fun findNoticeContent(noticeId: Long): NoticeContent? =
        noticeContentRepository.findByNoticeId(noticeId)

    override fun saveNoticeContent(noticeContent: NoticeContent): NoticeContent =
        noticeContentRepository.save(noticeContent)

    override fun delete(notice: Notice) {
        noticeContentRepository.deleteByNoticeId(requireNotNull(notice.id))
        noticeRepository.delete(notice)
    }

}
