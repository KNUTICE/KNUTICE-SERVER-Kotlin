package com.fx.global.exception.errorcode

import io.github.seob7.BaseErrorCode
import org.springframework.http.HttpStatus

enum class NotificationErrorCode(
    private val httpStatus: HttpStatus,
    private val message: String
) : BaseErrorCode {

    NOTIFICATION_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "알림 전송에 실패했습니다.")
    ;

    override fun getHttpStatus(): HttpStatus = httpStatus
    override fun getMessage(): String = message

}