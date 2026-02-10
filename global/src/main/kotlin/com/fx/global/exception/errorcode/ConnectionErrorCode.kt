package com.fx.global.exception.errorcode

import io.github.seob7.BaseErrorCode
import org.springframework.http.HttpStatus

enum class ConnectionErrorCode(
    private val httpStatus: HttpStatus,
    private val message: String
) : BaseErrorCode {

    REMOTE_SERVER_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "서비스에 연결할 수 없습니다.")
    ;

    override fun getHttpStatus(): HttpStatus = httpStatus
    override fun getMessage(): String = message

}