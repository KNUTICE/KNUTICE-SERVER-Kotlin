package com.fx.api.application.port.out

import com.fx.api.domain.NoticeQuery
import com.fx.global.domain.Notice

interface NoticePersistencePort {

    fun getNotices(noticeQuery: NoticeQuery): List<Notice>
    fun getNotice(nttId: Long): Notice?
    fun saveNotice(notice: Notice)
    fun deleteById(nttId: Long)
    fun existsById(nttId: Long): Boolean
    fun count(): Long
    fun countByContentSummaryExists(exists: Boolean): Long

}