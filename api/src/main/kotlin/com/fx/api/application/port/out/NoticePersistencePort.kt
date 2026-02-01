package com.fx.api.application.port.out

import com.fx.api.domain.NoticeQuery
import com.fx.global.domain.Notice
import java.time.LocalDateTime

interface NoticePersistencePort {

    fun getNotices(noticeQuery: NoticeQuery): List<Notice>
    fun getNotice(nttId: Long): Notice?
    fun saveNotice(notice: Notice)
    fun deleteById(nttId: Long)
    fun existsById(nttId: Long): Boolean

    fun countByCreatedAtLessThanEqual(dateTime: LocalDateTime): Long
    fun countByCreatedAtLessThanEqualAndHasSummary(dateTime: LocalDateTime): Long

}