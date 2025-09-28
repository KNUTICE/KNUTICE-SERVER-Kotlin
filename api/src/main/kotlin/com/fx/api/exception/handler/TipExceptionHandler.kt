package com.fx.api.exception.handler

import com.fx.api.exception.TipException
import io.github.seob7.Api
import io.github.seob7.BaseErrorCode
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class TipExceptionHandler {

    private val log = LoggerFactory.getLogger(TipExceptionHandler::class.java)

    @ExceptionHandler(TipException::class)
    fun handleTipException(e: TipException): ResponseEntity<Api<BaseErrorCode>> {
//        log.error("", e)
        return Api.ERROR(e.baseErrorCode)
    }

}