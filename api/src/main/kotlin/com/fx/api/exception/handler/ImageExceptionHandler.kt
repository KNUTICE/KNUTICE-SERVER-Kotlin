package com.fx.api.exception.handler

import com.fx.api.exception.ImageException
import io.github.seob7.Api
import io.github.seob7.BaseErrorCode
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ImageExceptionHandler {

    private val log = LoggerFactory.getLogger(ImageExceptionHandler::class.java)

    @ExceptionHandler(ImageException::class)
    fun handleImageException(e: ImageException): ResponseEntity<Api<BaseErrorCode>> {
        log.error("", e)
        return Api.ERROR(e.baseErrorCode)
    }

}