package com.fx.crawler.appllication.port.out

import com.fx.global.domain.Notice
import com.fx.global.domain.NoticeQuery

interface NoticePersistencePort {

    fun findByNttIdIn(nttIds: List<Long>): List<Notice>
    fun saveAll(notices: List<Notice>)
    fun getNotices(noticeQuery: NoticeQuery): List<Notice>

}