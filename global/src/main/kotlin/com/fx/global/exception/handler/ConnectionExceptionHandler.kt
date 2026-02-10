package com.fx.global.exception.handler

import com.fx.global.exception.ConnectionException
import io.github.seob7.Api
import io.github.seob7.BaseErrorCode
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ConnectionExceptionHandler {

    private val log = LoggerFactory.getLogger(ConnectionExceptionHandler::class.java)

    @ExceptionHandler(ConnectionException::class)
    fun handleConnectionException(e: ConnectionException): ResponseEntity<Api<BaseErrorCode>> {
        log.error("", e)
        return Api.ERROR(e.baseErrorCode)
    }

}