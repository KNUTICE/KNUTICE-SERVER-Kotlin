package com.fx.api.exception.handler

import com.fx.api.exception.JwtException
import com.fx.global.api.Api
import com.fx.global.api.BaseErrorCode
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class JwtExceptionHandler {

    private val log = LoggerFactory.getLogger(JwtExceptionHandler::class.java)

    @ExceptionHandler(JwtException::class)
    fun handleJwtException(e: JwtException): ResponseEntity<Api<BaseErrorCode>> {
        log.error("", e)
        return Api.ERROR(e.baseErrorCode)
    }
}