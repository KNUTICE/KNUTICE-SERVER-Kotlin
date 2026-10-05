package com.fx.common.exception.errorcode

import io.github.seob7.BaseErrorCode
import org.springframework.http.HttpStatus

enum class FcmTokenErrorCode(
    private val httpStatus: HttpStatus,
    private val message: String
) : BaseErrorCode {

    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "Fcm token 이 존재하지 않습니다."),
    TOKEN_INVALID(HttpStatus.BAD_REQUEST, "Fcm token 형식이 올바르지 않습니다."),
    LANGUAGE_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "지원하지 않는 언어입니다. ko, en, ja 중 하나여야 합니다.")
    ;

    override fun getHttpStatus(): HttpStatus =
        httpStatus
    override fun getMessage(): String =
        message

}