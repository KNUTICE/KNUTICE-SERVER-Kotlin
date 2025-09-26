package com.fx.api.exception.handler

import com.fx.api.exception.TopicException
import io.github.seob7.Api
import io.github.seob7.BaseErrorCode
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class TopicExceptionHandler {

    private val log = LoggerFactory.getLogger(TopicExceptionHandler::class.java)

    @ExceptionHandler(TopicException::class)
    fun handleTopicException(e: TopicException): ResponseEntity<Api<BaseErrorCode>> {
        log.error("", e)
        return Api.ERROR(e.baseErrorCode)
    }

}