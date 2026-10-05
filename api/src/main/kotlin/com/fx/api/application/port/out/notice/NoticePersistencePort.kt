package com.fx.api.application.port.out.notice

import com.fx.api.domain.NoticeQuery
import com.fx.common.domain.notice.Notice
import com.fx.common.domain.notice.NoticeContent
import com.fx.common.exception.NoticeException

interface NoticePersistencePort {

    fun findNotices(noticeQuery: NoticeQuery): List<Notice>

    /** @throws NoticeException 공지가 없을 때 (NOTICE_NOT_FOUND) */
    fun getNotice(nttId: Long): Notice

    fun existsByNttId(nttId: Long): Boolean

    /** 공지와 본문 · 요약 행을 함께 저장한다. */
    fun create(notice: Notice, contentSummary: String?): Notice

    fun findNoticeContent(noticeId: Long): NoticeContent?

    fun saveNoticeContent(noticeContent: NoticeContent): NoticeContent

    /** 공지와 본문 · 요약 행을 함께 지운다. */
    fun delete(notice: Notice)

}
