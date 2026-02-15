package com.fx.global.exception.handler

import com.fx.global.exception.ReadingRoomException
import io.github.seob7.Api
import io.github.seob7.BaseErrorCode
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ReadingRoomExceptionHandler {

    private val log = LoggerFactory.getLogger(ReadingRoomExceptionHandler::class.java)

    @ExceptionHandler(ReadingRoomException::class)
    fun handleReadingRoomException(e: ReadingRoomException): ResponseEntity<Api<BaseErrorCode>> {
        log.error("", e)
        return Api.ERROR(e.baseErrorCode)
    }

}