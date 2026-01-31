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

    fun countByCreatedAtBetween(start: LocalDateTime, end: LocalDateTime): Long
    fun countByCreatedAtBetweenAndHasSummary(start: LocalDateTime, end: LocalDateTime): Long


}