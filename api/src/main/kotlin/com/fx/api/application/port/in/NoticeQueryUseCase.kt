package com.fx.api.application.port.`in`

import com.fx.api.domain.NoticeQuery
import com.fx.global.domain.Notice

interface NoticeQueryUseCase {

    suspend fun getNotices(noticeQuery: NoticeQuery): List<Notice>
    suspend fun getNotice(nttId: Long): Notice
    suspend fun getNoticeSummary(nttId: Long): Notice

}