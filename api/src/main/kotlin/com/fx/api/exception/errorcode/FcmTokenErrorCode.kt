package com.fx.api.exception.errorcode

import io.github.seob7.BaseErrorCode
import org.springframework.http.HttpStatus

enum class FcmTokenErrorCode(
    private val httpStatus: HttpStatus,
    private val message: String
) : BaseErrorCode {

    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "Fcm token 이 존재하지 않습니다.")
    ;

    override fun getHttpStatus(): HttpStatus = httpStatus
    override fun getMessage(): String = message

}