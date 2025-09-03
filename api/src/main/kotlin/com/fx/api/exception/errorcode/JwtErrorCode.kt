package com.fx.api.exception.errorcode

import com.fx.global.api.BaseErrorCode
import org.springframework.http.HttpStatus

enum class JwtErrorCode(
    override val httpStatus: HttpStatus,
    override val message: String
) : BaseErrorCode {

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED, "알 수 없는 토큰 에러입니다.")

}