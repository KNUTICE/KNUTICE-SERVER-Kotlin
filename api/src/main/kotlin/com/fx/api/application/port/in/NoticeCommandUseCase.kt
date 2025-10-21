package com.fx.api.application.port.`in`

import com.fx.api.application.port.`in`.dto.NoticeCommand

interface NoticeCommandUseCase {

    fun saveNotice(noticeCommand: NoticeCommand): Boolean

    fun updateNotice(noticeCommand: NoticeCommand): Boolean

    fun deleteNotice(nttId: Long): Boolean

}