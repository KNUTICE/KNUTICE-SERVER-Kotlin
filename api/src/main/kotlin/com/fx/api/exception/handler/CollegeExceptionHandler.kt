package com.fx.api.exception.handler

import com.fx.api.exception.CollegeException
import io.github.seob7.Api
import io.github.seob7.BaseErrorCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class CollegeExceptionHandler {

    @ExceptionHandler(CollegeException::class)
    fun handleCollegeException(e: CollegeException): ResponseEntity<Api<BaseErrorCode>> =
        Api.ERROR(e.baseErrorCode)

}
