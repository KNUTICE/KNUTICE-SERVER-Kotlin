package com.fx.api.exception.errorcode

import com.fx.global.api.BaseErrorCode
import org.springframework.http.HttpStatus

enum class UserErrorCode(
    override val httpStatus: HttpStatus,
    override val message: String
) : BaseErrorCode
{
    EMAIL_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    NICKNAME_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 올바르지 않습니다."),
    USER_ID_NULL(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 ID가 존재하지 않습니다."),
    ROLE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "권한 정보가 없습니다."),
}