package com.fx.global.exception.handler

import com.fx.global.exception.MealException
import io.github.seob7.Api
import io.github.seob7.BaseErrorCode
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class MealExceptionHandler {

    private val log = LoggerFactory.getLogger(MealExceptionHandler::class.java)

    @ExceptionHandler(MealException::class)
    fun handleMealException(e: MealException): ResponseEntity<Api<BaseErrorCode>> {
        log.error("", e)
        return Api.ERROR(e.baseErrorCode)
    }

}