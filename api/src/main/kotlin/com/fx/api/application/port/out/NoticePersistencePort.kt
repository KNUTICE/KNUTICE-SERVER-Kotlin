package com.fx.api.application.port.out

import com.fx.api.domain.NoticeQuery
import com.fx.global.domain.Notice
import java.time.LocalDateTime

interface NoticePersistencePort {

    suspend fun getNotices(noticeQuery: NoticeQuery): List<Notice>
    suspend fun getNotice(nttId: Long): Notice?
    suspend fun saveNotice(notice: Notice)
    suspend fun deleteById(nttId: Long)
    suspend fun existsById(nttId: Long): Boolean

    suspend fun countByCreatedAtLessThanEqual(dateTime: LocalDateTime): Long
    suspend fun countByCreatedAtLessThanEqualAndHasSummary(dateTime: LocalDateTime): Long

}