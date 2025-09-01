package com.fx.api.exception.handler

import com.fx.api.exception.TipException
import com.fx.api.exception.TopicException
import com.fx.global.api.Api
import com.fx.global.api.ErrorCodeIfs
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class TopicExceptionHandler {

    private val log = LoggerFactory.getLogger(TopicExceptionHandler::class.java)

    @ExceptionHandler(TopicException::class)
    fun handleTopicException(e: TopicException): ResponseEntity<Api<ErrorCodeIfs>> {
        log.error("", e)
        return Api.ERROR(e.errorCodeIfs)
    }

}