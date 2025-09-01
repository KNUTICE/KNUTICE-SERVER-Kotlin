package com.fx.api.exception.handler

import com.fx.api.exception.NoticeException
import com.fx.global.api.Api
import com.fx.global.api.BaseErrorCode
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