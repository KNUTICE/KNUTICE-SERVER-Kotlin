package com.fx.global.exception.handler

import com.fx.global.exception.NoticeException
import io.github.seob7.Api
import io.github.seob7.BaseErrorCode
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class NoticeExceptionHandler {

    private val log = LoggerFactory.getLogger(NoticeExceptionHandler::class.java)

    @ExceptionHandler(NoticeException::class)
    fun handleNoticeException(e: NoticeException): ResponseEntity<Api<BaseErrorCode>> {
        log.error("", e)
        return Api.ERROR(e.baseErrorCode)
    }

}