package com.fx.crawler.appllication.port.out

import com.fx.global.domain.Notice
import java.time.LocalDateTime

interface NoticePersistencePort {

    suspend fun findByNttIdIn(nttIds: List<Long>): List<Notice>
    suspend fun saveAll(notices: List<Notice>)

    suspend fun countByCreatedAtLessThanEqual(dateTime: LocalDateTime): Long
    suspend fun countByCreatedAtLessThanEqualAndHasSummary(dateTime: LocalDateTime): Long

}