package com.fx.api.exception.errorcode

import com.fx.global.api.ErrorCodeIfs
import org.springframework.http.HttpStatus

enum class FcmTokenErrorCode(
    override val httpStatus: HttpStatus,
    override val message: String
) : ErrorCodeIfs {

    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "Fcm token 이 존재하지 않습니다.")

}