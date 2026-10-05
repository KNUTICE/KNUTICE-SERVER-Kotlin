package com.fx.common.exception.handler

import com.fx.common.exception.NoticeException
import com.fx.common.exception.NotificationException
import io.github.seob7.Api
import io.github.seob7.BaseErrorCode
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class NotificationExceptionHandler {

    private val log = LoggerFactory.getLogger(NotificationExceptionHandler::class.java)

    @ExceptionHandler(NotificationException::class)
    fun handleNotificationException(e: NotificationException): ResponseEntity<Api<BaseErrorCode>> {
        log.error("", e)
        return Api.ERROR(e.baseErrorCode)
    }

}