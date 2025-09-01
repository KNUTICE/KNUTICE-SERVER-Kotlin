package com.fx.global.exception.handler

import com.fx.global.api.Api
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import kotlin.collections.joinToString
import kotlin.jvm.java

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<Api<String>> {
        val errorMessage = e.bindingResult.fieldErrors.joinToString(" ") { fieldError ->
            "[${fieldError.field}](은)는 ${fieldError.defaultMessage} 입력된 값: [${fieldError.rejectedValue}]"
        }
        log.error("", e)
        return Api.ERROR(errorMessage, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(e: ConstraintViolationException): ResponseEntity<Api<String>> {
        val errorMessage = e.constraintViolations.joinToString(" ") { violation ->
            val field = violation.propertyPath.toString()
            val msg = violation.message
            "[${field}] ${msg} (입력된 값: ${violation.invalidValue})"
        }
        log.error("Validation error: {}", errorMessage, e)
        return Api.ERROR(errorMessage, HttpStatus.BAD_REQUEST)
    }

}