package com.fx.api.application.service

import com.fx.api.application.port.`in`.NoticeQueryUseCase
import com.fx.api.application.port.out.NoticePersistencePort
import com.fx.api.domain.NoticeQuery
import com.fx.api.exception.NoticeException
import com.fx.api.exception.errorcode.NoticeErrorCode
import com.fx.global.domain.Notice
import org.springframework.stereotype.Service

@Service
class NoticeQueryService(
    private val noticePersistencePort: NoticePersistencePort
): NoticeQueryUseCase {

    override fun getNotices(noticeQuery: NoticeQuery): List<Notice> {
        val notices = noticePersistencePort.getNotices(noticeQuery)
        if (notices.isEmpty()) {
            throw NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND)
        }
        return notices
    }

    override fun getNotice(nttId: Long): Notice =
        noticePersistencePort.getNotice(nttId)?: throw NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND)
}