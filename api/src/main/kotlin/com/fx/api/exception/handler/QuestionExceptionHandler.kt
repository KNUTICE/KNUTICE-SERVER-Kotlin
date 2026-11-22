package com.fx.api.exception.handler

import com.fx.api.exception.QuestionException
import io.github.seob7.Api
import io.github.seob7.BaseErrorCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class QuestionExceptionHandler {

    @ExceptionHandler(QuestionException::class)
    fun handleQuestionException(e: QuestionException): ResponseEntity<Api<BaseErrorCode>> =
        Api.ERROR(e.baseErrorCode)

}
