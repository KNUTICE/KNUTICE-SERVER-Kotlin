package com.fx.api.exception.handler

import com.fx.api.exception.AuthException
import io.github.seob7.Api
import io.github.seob7.BaseErrorCode
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/** 컨트롤러의 인증 오류와 보안 필터 체인의 인증 · 권한 오류(`AuthErrorResponder`)를 같은 형식으로 응답한다. */
@RestControllerAdvice
class AuthExceptionHandler {

    private val log = LoggerFactory.getLogger(AuthExceptionHandler::class.java)

    /** 토큰 만료 등 흔히 일어나는 클라이언트 오류라 스택 없이 남긴다. */
    @ExceptionHandler(AuthException::class)
    fun handleAuthException(e: AuthException): ResponseEntity<Api<BaseErrorCode>> {
        log.warn("인증 실패: {}", e.message)
        return Api.ERROR(e.baseErrorCode)
    }

}
