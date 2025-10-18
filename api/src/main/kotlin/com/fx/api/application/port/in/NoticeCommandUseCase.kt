package com.fx.api.application.port.`in`

import com.fx.api.application.port.`in`.dto.NoticeCommand

interface NoticeCommandUseCase {

    fun updateNotice(noticeCommand: NoticeCommand): Boolean

}