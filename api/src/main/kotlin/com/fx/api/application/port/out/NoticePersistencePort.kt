package com.fx.api.application.port.out

import com.fx.api.domain.NoticeQuery
import com.fx.global.domain.Notice

interface NoticePersistencePort {

    fun getNotices(noticeQuery: NoticeQuery): List<Notice>

}