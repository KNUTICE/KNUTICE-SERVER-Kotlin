package com.fx.api.application.port.`in`

import com.fx.global.domain.NoticeQuery
import com.fx.global.domain.Notice

interface NoticeQueryUseCase {

    fun getNotices(noticeQuery: NoticeQuery): List<Notice>
    fun getNotice(nttId: Long): Notice
    fun getNoticeSummary(nttId: Long): Notice

}