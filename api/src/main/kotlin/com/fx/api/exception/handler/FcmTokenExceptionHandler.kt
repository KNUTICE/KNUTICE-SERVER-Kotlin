package com.fx.api.exception.handler

import com.fx.api.exception.FcmTokenException
import com.fx.api.exception.NoticeException
import com.fx.global.api.Api
import com.fx.global.api.ErrorCodeIfs
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class FcmTokenExceptionHandler {

    private val log = LoggerFactory.getLogger(FcmTokenExceptionHandler::class.java)

    @ExceptionHandler(FcmTokenException::class)
    fun handleFcmTokenException(e: FcmTokenException): ResponseEntity<Api<ErrorCodeIfs>> {
        log.error("", e)
        return Api.ERROR(e.errorCodeIfs)
    }

}