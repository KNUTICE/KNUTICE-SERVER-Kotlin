package com.fx.api.application.port.`in`

import com.fx.api.application.port.`in`.dto.NoticeCommand

interface NoticeCommandUseCase {

    suspend fun saveNotice(noticeCommand: NoticeCommand): Boolean

    suspend fun updateNotice(noticeCommand: NoticeCommand): Boolean

    suspend fun deleteNotice(nttId: Long): Boolean

}