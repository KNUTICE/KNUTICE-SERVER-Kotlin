package com.fx.api.exception.errorcode

import io.github.seob7.BaseErrorCode
import org.springframework.http.HttpStatus

/**
 * 인증 · 권한 오류.
 *
 * 401 은 다시 인증하면 해결된다는 뜻이다. 관리자 화면은 액세스 토큰 오류(401)를 받으면 리프레시 토큰으로 재발급하고,
 * 재발급도 401 이면 로그인 화면으로 보낸다.
 */
enum class AuthErrorCode(
    private val httpStatus: HttpStatus,
    private val message: String
) : BaseErrorCode {

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "로그인이 만료되었습니다. 다시 로그인해 주세요."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    ;

    override fun getHttpStatus(): HttpStatus =
        httpStatus
    override fun getMessage(): String =
        message

}
