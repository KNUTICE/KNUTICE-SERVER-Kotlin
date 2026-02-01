package com.fx.crawler.appllication.port.out

import com.fx.global.domain.Notice
import java.time.LocalDateTime

interface NoticePersistencePort {

    fun findByNttIdIn(nttIds: List<Long>): List<Notice>
    fun saveAll(notices: List<Notice>)

    fun countByCreatedAtLessThanEqual(dateTime: LocalDateTime): Long
    fun countByCreatedAtLessThanEqualAndHasSummary(dateTime: LocalDateTime): Long

}