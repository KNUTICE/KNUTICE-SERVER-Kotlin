package com.fx.crawler.appllication.port.out

import com.fx.global.domain.Notice
import java.time.LocalDateTime

interface NoticePersistencePort {

    fun findByNttIdIn(nttIds: List<Long>): List<Notice>
    fun saveAll(notices: List<Notice>)

    fun countByCreatedAtBetween(start: LocalDateTime, end: LocalDateTime): Long
    fun countByCreatedAtBetweenAndHasSummary(start: LocalDateTime, end: LocalDateTime): Long

}