package com.fx.api.application.port.`in`

import com.fx.api.domain.NoticeQuery
import com.fx.global.domain.Notice

interface NoticeQueryUseCase {

    fun getNotices(noticeQuery: NoticeQuery): List<Notice>

}