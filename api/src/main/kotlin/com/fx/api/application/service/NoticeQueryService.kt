package com.fx.api.application.service

import com.fx.api.application.port.`in`.NoticeQueryUseCase
import com.fx.api.application.port.out.NoticePersistencePort
import com.fx.api.domain.NoticeQuery
import com.fx.global.domain.Notice
import org.springframework.stereotype.Service

@Service
class NoticeQueryService(
    private val noticePersistencePort: NoticePersistencePort
): NoticeQueryUseCase {

    override fun getNotices(noticeQuery: NoticeQuery): List<Notice> =
        noticePersistencePort.getNotices(noticeQuery)

}