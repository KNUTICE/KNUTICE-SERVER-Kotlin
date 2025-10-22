package com.fx.global.exception.handler

import com.fx.global.exception.FcmTokenException
import io.github.seob7.Api
import io.github.seob7.BaseErrorCode
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class FcmTokenExceptionHandler {

    private val log = LoggerFactory.getLogger(FcmTokenExceptionHandler::class.java)

    @ExceptionHandler(FcmTokenException::class)
    fun handleFcmTokenException(e: FcmTokenException): ResponseEntity<Api<BaseErrorCode>> {
        log.error("", e)
        return Api.ERROR(e.baseErrorCode)
    }

}