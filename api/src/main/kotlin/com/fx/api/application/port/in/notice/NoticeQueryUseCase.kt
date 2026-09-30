package com.fx.api.application.port.`in`.notice

import com.fx.api.application.port.`in`.notice.dto.NoticeSearchCommand
import com.fx.common.domain.notice.Notice

interface NoticeQueryUseCase {

    fun getNotices(noticeSearchCommand: NoticeSearchCommand): List<Notice>

    fun getNotice(nttId: Long): Notice

    /** 공지의 AI 요약. */
    fun getNoticeSummary(nttId: Long): String

}
